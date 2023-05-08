package us.tfg.ui.main
/*
Enum for signal:
    Excellent :signal > -90dBm
    Good : -91 to 105
    Fair : -106 to 110
    Poor : 111 to 115
    Very poor : 116 to 119
    Dead zone (no signal) : signal < -120
This is for 4G signal
     */

enum class Signal {
    EXCELLENT, GOOD, FAIR, POOR, VERY_POOR, DEAD_ZONE
}