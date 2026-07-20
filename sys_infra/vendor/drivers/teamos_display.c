#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include "teamos_display.h"

MODULE_LICENSE("GPL");
MODULE_AUTHOR("TeamOS Core Developers");
MODULE_DESCRIPTION("Display Driver Mock for TeamOS System Infrastructure");
MODULE_VERSION(DRIVER_VERSION);

static int __init teamos_display_init(void) {
    printk(KERN_INFO "[TeamOS Driver] %s version %s initialized successfully.\n", DRIVER_NAME, DRIVER_VERSION);
    return 0;
}

static void __exit teamos_display_exit(void) {
    printk(KERN_INFO "[TeamOS Driver] %s version %s unloaded.\n", DRIVER_NAME, DRIVER_VERSION);
}

module_init(teamos_display_init);
module_exit(teamos_display_exit);
