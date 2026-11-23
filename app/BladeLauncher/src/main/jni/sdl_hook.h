#pragma once

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct SDL_Window SDL_Window;

void set_real_SDL_CreateWindow(void *fn);
void set_real_SDL_CreateWindowWithProperties(void *fn);
void set_real_SDL_DestroyWindow(void *fn);

SDL_Window *dlsym_SDL_CreateWindow(const char *title, int w, int h, uint32_t flags);
SDL_Window *dlsym_SDL_CreateWindowWithProperties(uint32_t props);
void dlsym_SDL_DestroyWindow(SDL_Window *window);

#ifdef __cplusplus
}
#endif
