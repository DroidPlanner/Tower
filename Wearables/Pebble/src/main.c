#include <pebble.h>
#include <string.h>
#define APP_VERSION "three"
  
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!variables
static Window *window;
static TextLayer *mode_layer;
static TextLayer *telem_layer;
static TextLayer *follow_type_layer;
static Layer *buttons;
char *mode = "Stabilize";
char *follow_type = "";
int cam = 0;
enum {
  KEY_MODE = 0,
  KEY_FOLLOW_TYPE = 1,
  KEY_TELEM = 2,
  KEY_APP_VERSION=3,
  KEY_PEBBLE_REQUEST = 100,
  KEY_REQUEST_MODE_FOLLOW = 101,
  KEY_REQUEST_CYCLE_FOLLOW_TYPE=102,
  KEY_REQUEST_MODE_LOITER=103,
  KEY_REQUEST_MODE_RTL=104
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

static void send_mode_change_request(int request_type){
  Tuplet value = TupletInteger(KEY_PEBBLE_REQUEST,request_type);
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  
  dict_write_tuplet(iter,&value);
  app_message_outbox_send();
}

static void request_new_app_version(){
  text_layer_set_text(telem_layer,"Install new watchapp in settings");
  text_layer_set_text(mode_layer,"UPDATE!");
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!button click handlers
static void follow_click_handler(ClickRecognizerRef recognizer, void *context) {
  if(strcmp("Follow",mode)==0){
    vibe(30);
    send_mode_change_request(KEY_REQUEST_CYCLE_FOLLOW_TYPE);
  }else{
    vibe(100);
    send_mode_change_request(KEY_REQUEST_MODE_FOLLOW);
  }
}

static void loiter_click_handler(ClickRecognizerRef recognizer, void *context) {
  vibe(100);
  send_mode_change_request(KEY_REQUEST_MODE_LOITER);
}

static void RTL_handler(ClickRecognizerRef recognizer, void *context) {
  vibe(100);
  send_mode_change_request(KEY_REQUEST_MODE_RTL);
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!draw graphics
static void buttons_draw(Layer *layer, GContext *ctx) {
    GRect bounds = layer_get_bounds(layer);

    graphics_context_set_fill_color(ctx, GColorBlack);
    graphics_fill_rect(ctx, bounds, 10, GCornersLeft);
  
    graphics_context_set_text_color(ctx, GColorWhite);
    if(strcmp("Follow",mode)==0){
      graphics_draw_text(ctx, "Type",
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
    if(strcmp("Paused",mode)!=0)
    graphics_draw_text(ctx, "Pause",
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
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!data receiving
 void out_sent_handler(DictionaryIterator *sent, void *context) {
   // outgoing message was delivered
 }


 void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
   // outgoing message failed
 }


 void in_received_handler(DictionaryIterator *iter, void *context) {
   for(int i=KEY_MODE;i<=KEY_APP_VERSION;i++){
     Tuple *tuple = dict_find(iter,i);
     if(tuple){
       char *data = tuple->value->cstring;
       switch(i){
         case KEY_MODE:
           if(strcmp(data,mode)!=0)
             set_mode(data);
           break;
         case KEY_FOLLOW_TYPE:
         
           if(strcmp(data,follow_type)!=0){
             vibe(50);
             follow_type=data;
           }
           if(strcmp(mode,"Follow")==0)
               text_layer_set_text(follow_type_layer, follow_type);
           else
               text_layer_set_text(follow_type_layer, "");
         
           break;
         case KEY_TELEM:
           text_layer_set_text(telem_layer,data);
           break;
         case KEY_APP_VERSION:
           if(strcmp(data,APP_VERSION)!=0){
             request_new_app_version();
             return;
           }
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

  mode_layer = text_layer_create((GRect) { .origin = { 10, 0 }, .size = { bounds.size.w-50, 35 } });
  text_layer_set_text(mode_layer, "No Conn.");
  text_layer_set_text_alignment(mode_layer, GTextAlignmentLeft);
  text_layer_set_font(mode_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(mode_layer));
  
  follow_type_layer = text_layer_create((GRect) { .origin = { 10, 28 }, .size = { bounds.size.w-50, 35 } });//was 15+35+10
  text_layer_set_text(follow_type_layer, "");
  text_layer_set_text_alignment(follow_type_layer, GTextAlignmentLeft);
  text_layer_set_font(follow_type_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(follow_type_layer));
  
  telem_layer = text_layer_create((GRect) { .origin = { 10, 60 }, .size = { bounds.size.w-60, bounds.size.h-50 } });
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
