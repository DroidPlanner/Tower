#include <pebble.h>
#include <string.h>

  
//@TODO make cam a string, receive telem packets, send mode change packets, auto open on start
  
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!variables
static Window *window;
static TextLayer *mode_layer;
static TextLayer *telem_layer;
static TextLayer *camera_layer;
static Layer *buttons;
char *mode = "Stabilize";
int cam = 0;
enum {
  KEY_MODE = 0,
  KEW_FOLLOW_TYPE=1,
  KEY_TELEM_1 = 2,
  KEY_TELEM_2 = 3,
  KEY_TELEM_3 = 4,
  KEY_TELEM_4 = 5,
};
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!utils
static void vibe(int milliseconds){
  VibePattern custom_pattern = {
  .durations = (uint32_t []) {milliseconds},
  .num_segments = 1
};
  vibes_enqueue_custom_pattern(custom_pattern);
}

static void set_mode(char *str){
  vibe(100);
  mode = str;
  text_layer_set_text(mode_layer, str);
  if(strcmp("Follow",mode)!=0){
    text_layer_set_text(camera_layer, "");
  }
  layer_mark_dirty(buttons);
}
static void send_mode_change_request(char *requested_mode){
  return;
}
static void send_follow_type_cycle_request(){
  return;
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!button click handlers
static void follow_click_handler(ClickRecognizerRef recognizer, void *context) {
  if(strcmp("Follow",mode)==0){
    vibe(30);
    send_follow_type_cycle_request();
  }else{
    vibe(100);
    send_mode_change_request("Follow");
  }
}

static void loiter_click_handler(ClickRecognizerRef recognizer, void *context) {
  vibe(100);
  send_mode_change_request("Loiter");
}

static void RTL_handler(ClickRecognizerRef recognizer, void *context) {
  vibe(100);
  send_mode_change_request("RTL");
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!draw graphics
static void buttons_draw(Layer *layer, GContext *ctx) {
    GRect bounds = layer_get_bounds(layer);

    graphics_context_set_fill_color(ctx, GColorBlack);
    graphics_fill_rect(ctx, bounds, 10, GCornersLeft);
  
    graphics_context_set_text_color(ctx, GColorWhite);
    if(strcmp("Follow",mode)==0){
      graphics_draw_text(ctx, "Cam",
           fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
           GRect(0,5,50,30),
           GTextOverflowModeTrailingEllipsis,
           GTextAlignmentCenter,
           NULL);
    }else{
      graphics_draw_text(ctx, "Follow",
           fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
           GRect(0,5,50,30),
           GTextOverflowModeTrailingEllipsis,
           GTextAlignmentCenter,
           NULL);
    }
    if(strcmp("Loiter",mode)!=0)
    graphics_draw_text(ctx, "Loiter",
         fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
         GRect(0,55,50,30),
         GTextOverflowModeTrailingEllipsis,
         GTextAlignmentCenter,
         NULL);
    if(strcmp("RTL",mode)!=0)
    graphics_draw_text(ctx, "RTL",
         fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
         GRect(0,110,50,30),
         GTextOverflowModeTrailingEllipsis,
         GTextAlignmentCenter,
         NULL);
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!data sending
 void out_sent_handler(DictionaryIterator *sent, void *context) {
   // outgoing message was delivered
 }


 void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
   // outgoing message failed
 }


 void in_received_handler(DictionaryIterator *iter, void *context) {
   Tuple *mode_tuple = dict_find(iter, KEY_MODE);
   if(strcmp(mode_tuple->value->cstring,mode)!=0)//mode has changed, set new mode
     set_mode(mode_tuple->value->cstring);
   else{//otherwise, update whole telem string
     char telem[256];
     char *telem1 = dict_find(iter, KEY_TELEM_1)->value->cstring;
     char *telem2 = dict_find(iter, KEY_TELEM_2)->value->cstring;
     char *telem3 = dict_find(iter, KEY_TELEM_3)->value->cstring;
     char *telem4 = dict_find(iter, KEY_TELEM_4)->value->cstring;
     strcpy(telem,telem1);
     strcpy(telem,"\n");
     strcpy(telem,telem2);
     strcpy(telem,"\n");
     strcpy(telem,telem3);
     strcpy(telem,"\n");
     strcpy(telem,telem4);
     
     text_layer_set_text(telem_layer, telem);
   }
 }


 void in_dropped_handler(AppMessageResult reason, void *context) {
   // incoming message dropped
 }


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!initialization
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, loiter_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, follow_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, RTL_handler);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  mode_layer = text_layer_create((GRect) { .origin = { 10, 15 }, .size = { bounds.size.w-50, 35 } });
  text_layer_set_text(mode_layer, "Stabilize");
  text_layer_set_text_alignment(mode_layer, GTextAlignmentLeft);
  text_layer_set_font(mode_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(mode_layer));
  
  camera_layer = text_layer_create((GRect) { .origin = { 10, 15+35+10 }, .size = { bounds.size.w-50, 35 } });
  text_layer_set_text(camera_layer, "");
  text_layer_set_text_alignment(camera_layer, GTextAlignmentLeft);
  text_layer_set_font(camera_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(camera_layer));
  
  telem_layer = text_layer_create((GRect) { .origin = { 10, 100 }, .size = { bounds.size.w-50, 60 } });
  text_layer_set_text_alignment(telem_layer, GTextAlignmentLeft);
  text_layer_set_font(telem_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  layer_add_child(window_layer, text_layer_get_layer(telem_layer));
  
  buttons = layer_create((GRect) { .origin = { bounds.size.w-50, 5 }, .size = { bounds.size.w, bounds.size.h-10 } });
  layer_set_update_proc(buttons, buttons_draw);
  layer_add_child(window_layer, buttons);
}

static void window_unload(Window *window) {
  text_layer_destroy(mode_layer);
}

static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
	.load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(in_dropped_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_register_outbox_failed(out_failed_handler);
  const uint32_t inbound_size = 64;
  const uint32_t outbound_size = 64;
  app_message_open(inbound_size, outbound_size);
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}