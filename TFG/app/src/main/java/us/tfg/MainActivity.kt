package us.tfg

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import us.tfg.ui.main.SignalClass
import java.lang.reflect.Method
import java.util.*


class MainActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        val root = Runtime.getRuntime().exec("su")
    setDataEnabled(false)
        //encenderModoAvion()
    //apagarModoAvion()
    //setMobileDataState(false)
        //Settings.Global.putString(contentResolver, Settings.Global.DATA_ROAMING, "0");

/*


        val telephonyManager:TelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val dbm = SignalUtils.obtenerdBm(telephonyManager)
        println(dbm)
        println(SignalUtils.asignarCobertura(dbm))
 */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cm =  this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: Network? = cm.activeNetwork
        println(getNetworkClass(this))
        val timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateSignal()
            }
        }, 0, 1000)



       // Settings.System.putInt(contentResolver, Settings.System.  MOBILE_DATA, 1 );
        //Settings.System.putInt(contentResolver, Settings.System.DATA_ROAMING, 1 );
       // Settings.System.putInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0 );
    }



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun updateSignal(){
        val telephonyManager: TelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val signal = SignalUtils.obtenerdBm(telephonyManager)
        val dbm = findViewById<View>(R.id.dbm) as TextView
        dbm.text = signal.toString() +" dbm"
        val calidad = findViewById<View>(R.id.calidad) as TextView
        calidad.text =SignalUtils.asignarCobertura(signal, getNetworkClass(this)).toString()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun encenderModoAvion(){
        if(Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0){
            Settings.Global.putString(contentResolver, "airplane_mode_on", "1");

            Toast.makeText(applicationContext,"Modo avión desactivado",Toast.LENGTH_SHORT).show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun apagarModoAvion(){
        if(Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0){
            Toast.makeText(applicationContext,"Off",Toast.LENGTH_SHORT).show()
        } else {
            Settings.Global.putString(contentResolver, "airplane_mode_on", "0");
            Toast.makeText(applicationContext,"Modo avión desactivado",Toast.LENGTH_SHORT).show()
        }

    }

    fun getNetworkClass(context: Context): SignalClass? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        if (info == null || !info.isConnected) return SignalClass.NO_SIGNAL // not connected
        if (info.type == ConnectivityManager.TYPE_WIFI) return SignalClass.WIFI
        if (info.type == ConnectivityManager.TYPE_MOBILE) {
            val networkType = info.subtype
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> SignalClass.SIGNAL_2G
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> SignalClass.SIGNAL_3G
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> SignalClass.SIGNAL_4G
                TelephonyManager.NETWORK_TYPE_NR -> SignalClass.SIGNAL_5G
                else -> SignalClass.UNKNOWN
            }
        }
        return SignalClass.UNKNOWN
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setDataEnabled(enable: Boolean) {
        enforceModifyPermission()
        val telephonyManager:TelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.isDataEnabled = enable
    }
    private fun enforceModifyPermission() {

        super.enforceCallingOrSelfPermission(Manifest.permission.MODIFY_PHONE_STATE, "No se tiene permisos root")
    }

    fun setMobileDataState(mobileDataEnabled: Boolean) {
        try {
            val telephonyService = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val setMobileDataEnabledMethod: Method = Objects.requireNonNull(telephonyService).javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
            setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled)
        } catch (ex: Exception) {
            Log.e("MainActivity", "Error setting mobile data state", ex)
        }
    }

}