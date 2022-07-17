package us.tfg

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.system.ErrnoException
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import us.tfg.ui.main.Signal
import us.tfg.ui.main.SignalClass
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity(){
    private var ipAddress: String = "192.168.1.111"
    private var port: Int = 9090



    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        val root = Runtime.getRuntime().exec("su")
        setConnection(true)
            //setDataEnabled(false)

        // encenderModoAvion()
    //apagarModoAvion()
    //setMobileDataState(false)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(getNetworkClass(this))
        val timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                //ANALISIS DE COBERTURA
               val dbm = updateSignal()
                if(Signal.DEAD_ZONE.equals(dbm)){
                    println("conexion de mierda")
                }

            }
        }, 0, 1000)



       // Settings.System.putInt(contentResolver, Settings.System.  MOBILE_DATA, 1 );
        //Settings.System.putInt(contentResolver, Settings.System.DATA_ROAMING, 1 );
       // Settings.System.putInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0 );
    }


    //Metodo que actualiza los datos de la conexión movil
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun updateSignal(): Signal {
        val telephonyManager: TelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.networkOperator
        telephonyManager.networkOperatorName

        val signal = SignalUtils.obtenerdBm(telephonyManager)
        val dbm = findViewById<View>(R.id.dbm) as TextView
        dbm.text = signal.toString() +" dbm"
        val calidad = findViewById<View>(R.id.calidad) as TextView
        val sign = SignalUtils.asignarCobertura(signal, getNetworkClass(this))
        calidad.text =sign.toString()

        val datos: String =obtenerDatos(telephonyManager,signal.toString())

        try {
            sendMessage(datos)
        }catch ( e: Exception){
            println("Error al conectar al servidor")
        }

        return sign
    }

    //Método para obtener los datos de la cobertura y del dispositivo
    private fun obtenerDatos(telephonyManager: TelephonyManager, dbm: String): String {

        var datos:String
        //Añadimos datos de la cobertura
        datos = dbm
        val coordenadas = obtenerCoordenadas()
        datos += "&$coordenadas"
        val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {}
        datos += "&$currentDate"
        val tipoFrecuencia = getNetworkClass(this).toString()
        datos += "&$tipoFrecuencia"
        val operadora = telephonyManager.networkOperatorName
        datos += "&$operadora"

        //Añadimos datos del dispositivo
        datos += "&"+Build.MANUFACTURER //Fabricante
        datos += "&"+Build.MODEL //Modelo

        datos += "&"+Build.VERSION.RELEASE //Version de la release de Android
        datos += "&"+ Build.VERSION.SDK_INT.toString()  //Version SDK


        return datos

    }

    @SuppressLint("MissingPermission")
    private fun obtenerCoordenadas(): String {
        var locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
            }
       // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5f,this, Looper.getMainLooper() )
        var loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        var latitude: Double = loc?.latitude ?: 0.0
        var longitude: Double = loc?.longitude ?: 0.0
        return "$latitude,$longitude"

    }


    //Método para encender el modo avion
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun encenderModoAvion(){
        if(Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0){
            Settings.Global.putString(contentResolver, "airplane_mode_on", "1");
            Toast.makeText(applicationContext,"Modo avión desactivado",Toast.LENGTH_SHORT).show()
        }

    }

    //Método para apagar el modo avion
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun apagarModoAvion(){
        if(Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0){
            Toast.makeText(applicationContext,"Off",Toast.LENGTH_SHORT).show()
        } else {
            Settings.Global.putString(contentResolver, "airplane_mode_on", "0");
            Toast.makeText(applicationContext,"Modo avión desactivado",Toast.LENGTH_SHORT).show()
        }

    }

    //Método que nos devuelve que tipo de señal esta conectado
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

    private val COMMAND_L_ON = "svc data enable\n "
    private val COMMAND_L_OFF = "svc data disable\n "
    private val COMMAND_SU = "su"

    //Método que usando los comandos de root, activa o desactiva los datos moviles
    fun setConnection(enable: Boolean) {
        val command: String = if (enable) COMMAND_L_ON else COMMAND_L_OFF
        try {
            val su = Runtime.getRuntime().exec(COMMAND_SU)
            val outputStream = DataOutputStream(su.outputStream)
            outputStream.writeBytes(command)
            outputStream.flush()
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            try {
                su.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun sendMessage(message:String){
        val url = URL("http://$ipAddress:$port/cobertura/$message")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            inputStream.bufferedReader().use {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.lines().forEach { line ->
                        println(line)
                    }
                }
            }
        }
    }

}
