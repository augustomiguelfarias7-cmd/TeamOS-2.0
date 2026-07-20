#ifndef TEAMOS_DRIVERS_H
#define TEAMOS_DRIVERS_H

#define DRIVER_NAME "teamos_display"
#define DRIVER_VERSION "2.0.0"

typedef struct {
    int width;
    int height;
    int refresh_rate;
    const char* panel_name;
} TeamOSDisplayConfig;

#endif // TEAMOS_DRIVERS_H
