// SDL JNI_OnLoad isolation for the embedded game JVM.
// Reference: https://github.com/AngelAuraMC/Amethyst-Android/commit/e4c79084d

#include <dlfcn.h>
#include <jni.h>
#include <bytehook.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdbool.h>

#include "environ/environ.h"
#include "logger/logger.h"
#include "utils.h"
#include "sdl_hook.h"

typedef void *(*dlsym_func_t)(void *handle, const char *symbol);
typedef jint (*jni_on_load_func_t)(JavaVM *vm, void *reserved);

typedef bytehook_stub_t (*bytehook_hook_all_fn)(const char *callee_path_name, const char *symbol,
                                                  void *new_func, bytehook_hooked_t hooked, void *hooked_arg);

static void *sdlHandle;
static jni_on_load_func_t originalSdlJniOnLoad;

static jint isolatedSdlJniOnLoad(JavaVM *vm, void *reserved) {
    if (originalSdlJniOnLoad != NULL && pojav_environ->dalvikJavaVMPtr == vm) {
        return originalSdlJniOnLoad(vm, reserved);
    }
    return JNI_VERSION_1_4;
}

static void *dummy_generic_stub(void) {
    return NULL;
}

static float dummy_float_stub(void) {
    return 0.0f;
}

static int dummy_pagesize_stub(void) {
    return 4096;
}

static bool dummy_true_stub(void) {
    return true;
}

typedef bool (*sdl_gl_load_library_func_t)(const char *path);
typedef void *(*sdl_gl_get_proc_address_func_t)(const char *proc);

static sdl_gl_get_proc_address_func_t original_SDL_GL_GetProcAddress;

static void *sGlGetErrorAddress = NULL;
static void *sGlLibraryHandle = NULL;

static __thread bool sInDlsym = false;

static void *resolve_real_glGetError(void) {
    if (sGlGetErrorAddress != NULL) {
        return sGlGetErrorAddress;
    }
    sInDlsym = true;
    if (sGlLibraryHandle != NULL) {
        void *sym = dlsym(sGlLibraryHandle, "glGetError");
        if (sym != NULL) {
            sGlGetErrorAddress = sym;
            sInDlsym = false;
            return sym;
        }
    }
    const char *candidates[] = {
        "libng_gl4es.so",
        "libgl4es_114.so",
        "libGL.so",
        "libGL.so.1",
        NULL
    };
    for (int i = 0; candidates[i] != NULL; i++) {
        void *h = dlopen(candidates[i], RTLD_LOCAL | RTLD_LAZY);
        if (h != NULL) {
            void *sym = dlsym(h, "glGetError");
            if (sym != NULL) {
                sGlGetErrorAddress = sym;
                if (sGlLibraryHandle == NULL) {
                    sGlLibraryHandle = h;
                }
                break;
            }
        }
    }
    sInDlsym = false;
    return sGlGetErrorAddress;
}

static void *custom_SDL_GL_GetProcAddress(const char *proc);
static void *dlsym_SDL_GL_GetProcAddress(const char *proc);

static void *internal_SDL_GL_GetProcAddress(const char *proc) {
    if (proc != NULL && strcmp(proc, "glGetError") == 0) {
        void *errSym = resolve_real_glGetError();
        if (errSym != NULL) {
            return errSym;
        }
    }

    if (original_SDL_GL_GetProcAddress == NULL) {
        if (sdlHandle == NULL) {
            sdlHandle = dlopen("libSDL3.so", RTLD_LOCAL | RTLD_NOW);
        }
        if (sdlHandle != NULL) {
            sInDlsym = true;
            original_SDL_GL_GetProcAddress = (sdl_gl_get_proc_address_func_t) dlsym(sdlHandle, "SDL_GL_GetProcAddress");
            sInDlsym = false;
        }
    }

    if (original_SDL_GL_GetProcAddress != NULL &&
        original_SDL_GL_GetProcAddress != (sdl_gl_get_proc_address_func_t) custom_SDL_GL_GetProcAddress &&
        original_SDL_GL_GetProcAddress != (sdl_gl_get_proc_address_func_t) dlsym_SDL_GL_GetProcAddress) {
        void *sym = original_SDL_GL_GetProcAddress(proc);
        if (sym != NULL) {
            return sym;
        }
    }

    if (proc != NULL) {
        if (sGlLibraryHandle != NULL) {
            sInDlsym = true;
            void *sym = dlsym(sGlLibraryHandle, proc);
            sInDlsym = false;
            if (sym != NULL) {
                return sym;
            }
        }
        sInDlsym = true;
        void *sym = dlsym(RTLD_DEFAULT, proc);
        sInDlsym = false;
        if (sym != NULL) {
            return sym;
        }
    }
    return NULL;
}

static void *custom_SDL_GL_GetProcAddress(const char *proc) {
    BYTEHOOK_POP_STACK();
    return internal_SDL_GL_GetProcAddress(proc);
}

static void *dlsym_SDL_GL_GetProcAddress(const char *proc) {
    return internal_SDL_GL_GetProcAddress(proc);
}

static void *customDlsym(void *handle, const char *symbol) {
    void *result = BYTEHOOK_CALL_PREV(customDlsym, dlsym_func_t, handle, symbol);
    BYTEHOOK_POP_STACK();

    if (sInDlsym) {
        return result;
    }

    if (symbol != NULL && strcmp(symbol, "glGetError") == 0) {
        if (result != NULL) {
            sGlGetErrorAddress = result;
            sGlLibraryHandle = handle;
        }
    }

    if (sdlHandle == NULL) {
        sdlHandle = dlopen("libSDL3.so", RTLD_LOCAL | RTLD_NOW);
    }
    if (sdlHandle != NULL && handle == sdlHandle && symbol != NULL && strcmp(symbol, "JNI_OnLoad") == 0) {
        originalSdlJniOnLoad = (jni_on_load_func_t) result;
        result = (void *) isolatedSdlJniOnLoad;
    }
    if (symbol != NULL && strcmp(symbol, "SDL_GL_GetProcAddress") == 0) {
        if (result != NULL && result != (void *) custom_SDL_GL_GetProcAddress && result != (void *) dlsym_SDL_GL_GetProcAddress) {
            original_SDL_GL_GetProcAddress = (sdl_gl_get_proc_address_func_t) result;
        }
        return (void *) dlsym_SDL_GL_GetProcAddress;
    }
    if (symbol != NULL && strcmp(symbol, "SDL_CreateWindow") == 0) {
        set_real_SDL_CreateWindow(result);
        return (void *) dlsym_SDL_CreateWindow;
    }
    if (symbol != NULL && strcmp(symbol, "SDL_CreateWindowWithProperties") == 0) {
        set_real_SDL_CreateWindowWithProperties(result);
        return (void *) dlsym_SDL_CreateWindowWithProperties;
    }
    if (symbol != NULL && strcmp(symbol, "SDL_DestroyWindow") == 0) {
        set_real_SDL_DestroyWindow(result);
        return (void *) dlsym_SDL_DestroyWindow;
    }
    if (result == NULL && symbol != NULL && strncmp(symbol, "SDL_", 4) == 0) {
        if (strcmp(symbol, "SDL_GetWindowProgressValue") == 0) {
            result = (void *) dummy_float_stub;
        } else if (strcmp(symbol, "SDL_GetSystemPageSize") == 0) {
            result = (void *) dummy_pagesize_stub;
        } else if (strcmp(symbol, "SDL_SDL_RegisterApp") == 0 ||
                   strcmp(symbol, "SDL_SetWindowFillDocument") == 0) {
            result = (void *) dummy_true_stub;
        } else {
            result = (void *) dummy_generic_stub;
        }
        LOG_TO_I("customDlsym: Provided stub for missing SDL3 symbol '%s'", symbol);
    }
    return result;
}

void create_sdl_dlopen_hooks(bytehook_hook_all_fn hookAll) {
    if (hookAll == NULL) return;
    hookAll(NULL, "dlsym", (void *) customDlsym, NULL, NULL);
    hookAll(NULL, "SDL_GL_GetProcAddress", (void *) custom_SDL_GL_GetProcAddress, NULL, NULL);
}