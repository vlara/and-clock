package com.trbowrx.pi_clock;

// Font data from https://github.com/adafruit/Adafruit_LED_Backpack

import java.util.HashMap;
import java.util.Map;

/***************************************************
 This is a library for our I2C LED Backpacks
 Designed specifically to work with the Adafruit LED Matrix backpacks
 ----> http://www.adafruit.com/products/
 ----> http://www.adafruit.com/products/
 These displays use I2C to communicate, 2 pins are required to
 interface. There are multiple selectable I2C addresses. For backpacks
 with 2 Address Select pins: 0x70, 0x71, 0x72 or 0x73. For backpacks
 with 3 Address Select pins: 0x70 thru 0x77
 Adafruit invests time and resources providing this open source code,
 please support Adafruit and open-source hardware by purchasing
 products from Adafruit!
 Written by Limor Fried/Ladyada for Adafruit Industries.
 MIT license, all text above must be included in any redistribution
 ****************************************************/

class Font {

    static final Map<String,Integer> myMap;

    static
    {
        myMap = new HashMap<String, Integer>();
        myMap.put("0", 0x3F);
        myMap.put("1", 0x06);
        myMap.put("2", 0x5B);
        myMap.put("3", 0x4F);
        myMap.put("4", 0x66);
        myMap.put("5", 0x6D);
        myMap.put("6", 0x7D);
        myMap.put("7", 0x07);
        myMap.put("8", 0x7F);
        myMap.put("9", 0x6F);
    }
}