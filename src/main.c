#include <pebble.h>
#include <string.h>

  
//@TODO make cam a string, receive telem packets, send mode change packets, auto open on start
  
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!variables
static Window *window;
static TextLayer *mode_layer;
static TextLayer *telem_layer;
static TextLayer *follow_type_layer;
static Layer *buttons;
char *mode = "Stabilize";
int cam = 0;
enum {
  KEY_MODE = 0,
  KEY_FOLLOW_TYPE = 1,
  KEY_TELEM = 2
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
  strncpy(mode,str,10);
  text_layer_set_text(mode_layer, str);
  if(strcmp("Follow",mode)!=0){
    text_layer_set_text(follow_type_layer, "");
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
   for(int i=KEY_MODE;i<=KEY_TELEM;i++){
     Tuple *tuple = dict_find(iter,i);
     if(tuple){
       char *data = tuple->value->cstring;
       switch(i){
         case KEY_MODE:
           if(strcmp(data,mode)!=0)
             set_mode(data);
           break;
         case KEY_FOLLOW_TYPE:
           
           break;
         case KEY_TELEM:
           text_layer_set_text(telem_layer,data);
           break;
       }
     }
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

  mode_layer = text_layer_create((GRect) { .origin = { 10, 5 }, .size = { bounds.size.w-50, 35 } });
  text_layer_set_text(mode_layer, "No Conn.");
  text_layer_set_text_alignment(mode_layer, GTextAlignmentLeft);
  text_layer_set_font(mode_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(mode_layer));
  
  follow_type_layer = text_layer_create((GRect) { .origin = { 10, 15+35+10 }, .size = { bounds.size.w-50, 35 } });
  text_layer_set_text(follow_type_layer, "");
  text_layer_set_text_alignment(follow_type_layer, GTextAlignmentLeft);
  text_layer_set_font(follow_type_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(follow_type_layer));
  
  telem_layer = text_layer_create((GRect) { .origin = { 10, 50 }, .size = { bounds.size.w-60, bounds.size.h-50 } });
  text_layer_set_overflow_mode(telem_layer, GTextOverflowModeWordWrap);
  text_layer_set_text(telem_layer, "No telem. yet");
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
  const uint32_t inbound_size = 128;
  const uint32_t outbound_size = 16;
  app_message_open(inbound_size, outbound_size);
  app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
}

static void deinit(void) {
  window_destroy(window);
  app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}