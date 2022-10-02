package us.tfg

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import us.tfg.ui.main.Signal
import us.tfg.ui.main.SignalClass

object SignalUtils{
    /*
Método que obtiene la fuerza de la cobertura en dbm
 */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
      fun obtenerdBm(telephonyManager: TelephonyManager): Int?{
        val signalStrength: SignalStrength? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephonyManager.signalStrength
        } else {
            TODO("VERSION.SDK_INT < P")
        }
        val dbm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            signalStrength?.cellSignalStrengths?.get(0)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        return dbm?.dbm
    }

    fun asignarCobertura(dbm:Int?, signal:SignalClass?): Signal{
        if (signal == SignalClass.SIGNAL_4G){
            if (dbm != null) {
                if (dbm>=-90){
                    return Signal.EXCELLENT
                }else if (dbm<=-91 && dbm >= -105){
                    return Signal.GOOD
                }else if(dbm <= -106 && dbm >= -110){
                    return Signal.FAIR
                }else if(dbm <=-111 && dbm > -115){
                    return Signal.POOR
                }else if(dbm <=-116 && dbm >-120){
                    return Signal.VERY_POOR
                }else if(dbm <= -120){
                    return Signal.DEAD_ZONE
                }else{
                    return Signal.DEAD_ZONE
                }
            }else{
                return Signal.DEAD_ZONE
            }
        }else if(signal == SignalClass.SIGNAL_3G || signal == SignalClass.SIGNAL_2G){
            if (dbm != null) {
                if (dbm>=-70){
                    return Signal.EXCELLENT
                }else if (dbm<=-71 && dbm >= -85){
                    return Signal.GOOD
                }else if(dbm <= -86 && dbm >= -100){
                    return Signal.FAIR
                }else if(dbm <=-101 && dbm > -105){
                    return Signal.POOR
                }else if(dbm <=-106 && dbm >-110){
                    return Signal.VERY_POOR
                }else if(dbm <= -110){
                    return Signal.DEAD_ZONE
                }else{
                    return Signal.DEAD_ZONE
                }
            }else{
                return Signal.DEAD_ZONE
            }
        }else{return Signal.DEAD_ZONE}
    }
}