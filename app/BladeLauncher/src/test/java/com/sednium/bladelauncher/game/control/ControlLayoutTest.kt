package com.sednium.bladelauncher.game.control

import com.movtery.layer_controller.layout.loadLayoutFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ControlLayoutTest {

    @Test
    fun testPojavConvertedLayoutLoadsSuccessfully() {
        val layout = loadLayoutFromString(POJAV_CONTROLS_JSON)
        assertNotNull(layout)
        assertEquals("Pojav Controls", layout.info.name.default)
        assertEquals(1, layout.layers.size)
        assertEquals(21, layout.layers[0].normalButtons.size)
    }

    companion object {
        private val POJAV_CONTROLS_JSON = """
{
    "info": {
        "name": {
            "default": "Pojav Controls",
            "matchQueue": [
                {
                    "language_tag": "zh",
                    "value": "Pojav 键位"
                }
            ]
        },
        "author": {
            "default": "Imported",
            "matchQueue": []
        },
        "description": {
            "default": "Converted from PojavLauncher layout",
            "matchQueue": []
        },
        "versionCode": 0,
        "versionName": "1.0"
    },
    "layers": [
        {
            "name": "main",
            "uuid": "layer_4bb523a9549b",
            "hide": false,
            "hideWhenMouse": true,
            "hideWhenGamepad": true,
            "hideWhenJoystick": false,
            "visibilityType": "always",
            "normalButtons": [
                {
                    "text": {
                        "default": "Keyboard",
                        "matchQueue": []
                    },
                    "uuid": "157c90654df6455b89",
                    "position": {
                        "x": 2377,
                        "y": 346
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "launcher_event",
                            "key": "launcher.event.switch_ime"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "GUI",
                        "matchQueue": []
                    },
                    "uuid": "9e684d18e1734e73b1",
                    "position": {
                        "x": 100,
                        "y": 9814
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "launcher_event",
                            "key": "launcher.event.switch_menu"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "PRI",
                        "matchQueue": []
                    },
                    "uuid": "a331f3ab94114f67bd",
                    "position": {
                        "x": 100,
                        "y": 7116
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "launcher_event",
                            "key": "GLFW_MOUSE_BUTTON_LEFT"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "SEC",
                        "matchQueue": []
                    },
                    "uuid": "7b0fd43177584daeaf",
                    "position": {
                        "x": 1542,
                        "y": 7116
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "launcher_event",
                            "key": "GLFW_MOUSE_BUTTON_RIGHT"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Mouse",
                        "matchQueue": []
                    },
                    "uuid": "2afd85eb9a4e4a5eb6",
                    "position": {
                        "x": 9896,
                        "y": 357
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_F3"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Debug",
                        "matchQueue": []
                    },
                    "uuid": "64321abe0a4b4c528e",
                    "position": {
                        "x": 103,
                        "y": 346
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_F3"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Chat",
                        "matchQueue": []
                    },
                    "uuid": "278f12172a734b598d",
                    "position": {
                        "x": 1240,
                        "y": 346
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_T"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Tab",
                        "matchQueue": []
                    },
                    "uuid": "8ca10f07622c466180",
                    "position": {
                        "x": 3514,
                        "y": 346
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_TAB"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "3rd",
                        "matchQueue": []
                    },
                    "uuid": "74f66e90ef7a46d2ab",
                    "position": {
                        "x": 103,
                        "y": 1190
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 80.0,
                        "heightDp": 30.0,
                        "widthPercentage": 1667,
                        "heightPercentage": 625,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_F5"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "▲",
                        "matchQueue": []
                    },
                    "uuid": "63c6a4d43eda4bfdad",
                    "position": {
                        "x": 821,
                        "y": 7116
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_W"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "◀",
                        "matchQueue": []
                    },
                    "uuid": "fe1047c24fd0476680",
                    "position": {
                        "x": 100,
                        "y": 8465
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_A"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "▼",
                        "matchQueue": []
                    },
                    "uuid": "f5478cbdda954238a1",
                    "position": {
                        "x": 821,
                        "y": 9814
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_S"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "▶",
                        "matchQueue": []
                    },
                    "uuid": "f2701b78da7746a186",
                    "position": {
                        "x": 1542,
                        "y": 8465
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_D"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Inv",
                        "matchQueue": []
                    },
                    "uuid": "92208479d0034d0f84",
                    "position": {
                        "x": 1542,
                        "y": 9814
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_E"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "◇",
                        "matchQueue": []
                    },
                    "uuid": "1810fc22fcc149ceac",
                    "position": {
                        "x": 821,
                        "y": 8465
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_LEFT_SHIFT"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": true
                },
                {
                    "text": {
                        "default": "⬛",
                        "matchQueue": []
                    },
                    "uuid": "624aef31f0904fdbad",
                    "position": {
                        "x": 8717,
                        "y": 8026
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_SPACE"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "Q",
                        "matchQueue": []
                    },
                    "uuid": "6b82dd80eb5a4f1abb",
                    "position": {
                        "x": 9900,
                        "y": 1257
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_Q"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "C",
                        "matchQueue": []
                    },
                    "uuid": "c256fc2ca15946e79f",
                    "position": {
                        "x": 9179,
                        "y": 1257
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_C"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "5",
                        "matchQueue": []
                    },
                    "uuid": "3fe53db2879640c880",
                    "position": {
                        "x": 9179,
                        "y": 2606
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_5"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "M",
                        "matchQueue": []
                    },
                    "uuid": "b6e96ef2f66f451289",
                    "position": {
                        "x": 9900,
                        "y": 2606
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_M"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                },
                {
                    "text": {
                        "default": "R",
                        "matchQueue": []
                    },
                    "uuid": "be2ddd8de79e43be93",
                    "position": {
                        "x": 9900,
                        "y": 3955
                    },
                    "buttonSize": {
                        "type": "dp",
                        "widthDp": 50.0,
                        "heightDp": 50.0,
                        "widthPercentage": 1042,
                        "heightPercentage": 1042,
                        "widthReference": "screen_height",
                        "heightReference": "screen_height"
                    },
                    "buttonStyle": "d1096cf91caa",
                    "visibilityType": "always",
                    "clickEvents": [
                        {
                            "type": "key",
                            "key": "GLFW_KEY_R"
                        }
                    ],
                    "isSwipple": false,
                    "isPenetrable": false,
                    "isToggleable": false
                }
            ],
            "textBoxes": []
        }
    ],
    "styles": [
        {
            "name": "default",
            "uuid": "d1096cf91caa",
            "animateSwap": false,
            "commonStyle": true,
            "lightStyle": {
                "alpha": 1.0,
                "pressedAlpha": 1.0,
                "backgroundColor": -9223372036854775808,
                "pressedBackgroundColor": -5510004026390872064,
                "contentColor": -4294967296,
                "pressedContentColor": -4294967296,
                "borderWidth": 0,
                "pressedBorderWidth": 0,
                "borderColor": -4294967296,
                "pressedBorderColor": -4294967296,
                "borderRadius": {
                    "topStart": 0.0,
                    "topEnd": 0.0,
                    "bottomEnd": 0.0,
                    "bottomStart": 0.0
                },
                "pressedBorderRadius": {
                    "topStart": 0.0,
                    "topEnd": 0.0,
                    "bottomEnd": 0.0,
                    "bottomStart": 0.0
                }
            },
            "darkStyle": {
                "alpha": 1.0,
                "pressedAlpha": 1.0,
                "backgroundColor": -9223372036854775808,
                "pressedBackgroundColor": -5510004026390872064,
                "contentColor": -4294967296,
                "pressedContentColor": -4294967296,
                "borderWidth": 0,
                "pressedBorderWidth": 0,
                "borderColor": -4294967296,
                "pressedBorderColor": -4294967296,
                "borderRadius": {
                    "topStart": 0.0,
                    "topEnd": 0.0,
                    "bottomEnd": 0.0,
                    "bottomStart": 0.0
                },
                "pressedBorderRadius": {
                    "topStart": 0.0,
                    "topEnd": 0.0,
                    "bottomEnd": 0.0,
                    "bottomStart": 0.0
                }
            }
        }
    ],
    "editorVersion": 11
}
""".trimIndent()
    }
}
