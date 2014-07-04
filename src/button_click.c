#include <pebble.h>

static Window *window;
static TextLayer *mode_layer;
static TextLayer *telem_layer;
static TextLayer *camera_layer;
static Layer *buttons;
bool is_follow = false;
int cam = 0;

static void follow_click_handler(ClickRecognizerRef recognizer, void *context) {
  vibes_double_pulse();
  text_layer_set_text(mode_layer, "Follow");
  if(is_follow){
    vibes_short_pulse();
    cam++;
  }
  if(cam>2){
    cam=0;
  }
  switch(cam){
  case 0:
    text_layer_set_text(camera_layer, "behind");
    break;
  case 1:
    text_layer_set_text(camera_layer, "above");
    break;
  case 2:
    text_layer_set_text(camera_layer, "circle");
    break;
}
  is_follow=true;
  //layer_mark_dirty(buttons->layer);
}

static void loiter_click_handler(ClickRecognizerRef recognizer, void *context) {
  vibes_double_pulse();
  text_layer_set_text(mode_layer, "Loiter");
  text_layer_set_text(camera_layer, "");
  is_follow=false;
}

static void RTL_handler(ClickRecognizerRef recognizer, void *context) {
  vibes_double_pulse();
  text_layer_set_text(mode_layer, "RTL");
  text_layer_set_text(camera_layer, "");
  is_follow=false;
}

static void buttons_draw(Layer *layer, GContext *ctx) {
    GRect bounds = layer_get_bounds(layer);

    // Draw a black filled rectangle with sharp corners
    graphics_context_set_fill_color(ctx, GColorBlack);
    graphics_fill_rect(ctx, bounds, 10, GCornersLeft);
  
    graphics_context_set_text_color(ctx, GColorWhite);
  
    graphics_draw_text(ctx, "Follow",
         fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
         GRect(0,5,50,30),
         GTextOverflowModeTrailingEllipsis,
         GTextAlignmentCenter,
         NULL);
  
    graphics_draw_text(ctx, "Loiter",
         fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
         GRect(0,55,50,30),
         GTextOverflowModeTrailingEllipsis,
         GTextAlignmentCenter,
         NULL);
  
    graphics_draw_text(ctx, "RTL",
         fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD),
         GRect(0,110,50,30),
         GTextOverflowModeTrailingEllipsis,
         GTextAlignmentCenter,
         NULL);
}

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
  
  telem_layer = text_layer_create((GRect) { .origin = { 10, 110 }, .size = { bounds.size.w-50, 50 } });
  text_layer_set_text(telem_layer, "Alt: 232m\nBat:10.2V");
  text_layer_set_text_alignment(telem_layer, GTextAlignmentLeft);
  text_layer_set_font(telem_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18));
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
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}