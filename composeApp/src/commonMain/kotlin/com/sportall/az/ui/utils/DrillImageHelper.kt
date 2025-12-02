package com.sportall.az.ui.utils

import com.sportall.az.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Helper function to dynamically load drill images by ID
 * Maps drill IDs to their corresponding drawable resources
 */
fun getDrillImageResource(drillId: Int): DrawableResource? = when(drillId) {
    1 -> Res.drawable.drill_1
    2 -> Res.drawable.drill_2
    3 -> Res.drawable.drill_3
    4 -> Res.drawable.drill_4
    5 -> Res.drawable.drill_5
    6 -> Res.drawable.drill_6
    7 -> Res.drawable.drill_7
    8 -> Res.drawable.drill_8
    9 -> Res.drawable.drill_9
    10 -> Res.drawable.drill_10
    11 -> Res.drawable.drill_11
    12 -> Res.drawable.drill_12
    13 -> Res.drawable.drill_13
    14 -> Res.drawable.drill_14
    15 -> Res.drawable.drill_15
    16 -> Res.drawable.drill_16
    17 -> Res.drawable.drill_17
    18 -> Res.drawable.drill_18
    19 -> Res.drawable.drill_19
    20 -> Res.drawable.drill_20
    21 -> Res.drawable.drill_21
    22 -> Res.drawable.drill_22
    23 -> Res.drawable.drill_23
    24 -> Res.drawable.drill_24
    25 -> Res.drawable.drill_25
    26 -> Res.drawable.drill_26
    27 -> Res.drawable.drill_27
    28 -> Res.drawable.drill_28
    29 -> Res.drawable.drill_29
    30 -> Res.drawable.drill_30
    31 -> Res.drawable.drill_31
    32 -> Res.drawable.drill_32
    33 -> Res.drawable.drill_33
    34 -> Res.drawable.drill_34
    35 -> Res.drawable.drill_35
    36 -> Res.drawable.drill_36
    37 -> Res.drawable.drill_37
    38 -> Res.drawable.drill_38
    39 -> Res.drawable.drill_39
    40 -> Res.drawable.drill_40
    41 -> Res.drawable.drill_41
    42 -> Res.drawable.drill_42
    43 -> Res.drawable.drill_43
    44 -> Res.drawable.drill_44
    45 -> Res.drawable.drill_45
    46 -> Res.drawable.drill_46
    47 -> Res.drawable.drill_47
    48 -> Res.drawable.drill_48
    49 -> Res.drawable.drill_49
    50 -> Res.drawable.drill_50
    51 -> Res.drawable.drill_51
    52 -> Res.drawable.drill_52
    53 -> Res.drawable.drill_53
    54 -> Res.drawable.drill_54
    55 -> Res.drawable.drill_55
    else -> null
}
