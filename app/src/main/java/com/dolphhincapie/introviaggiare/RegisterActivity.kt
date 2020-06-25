package com.dolphhincapie.introviaggiare

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var fecha: String
    private var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)

                val format = "dd/MM/yy"
                val simpleDateFormat = SimpleDateFormat(format, Locale.US)
                fecha = simpleDateFormat.format(cal.time).toString()
                tv_calendar.text = fecha
            }

        ib_calendar.setOnClickListener {
            DatePickerDialog( this,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        bt_guardar.setOnClickListener {
            val nombre = et_nombre.text.toString()
            val correo = et_correo.text.toString()
            val contrasena = et_contrasena.text.toString()
            val repContra = et_repitacontrasena.text.toString()
            val genero = if(rb_masculino.isChecked) "Masculino" else "Femenino"
            var pasatiempos = ""
            val ciudadNacimiento = s_ciudades.selectedItem.toString()
            val fechanaci = tv_calendar.text.toString()


            if (cb_cine.isChecked) pasatiempos = "$pasatiempos Cine"
            if (cb_videojuegos.isChecked) pasatiempos = "$pasatiempos VideoJuegos"
            if (cb_series.isChecked) pasatiempos = "$pasatiempos Series"
            if (cb_deportes.isChecked) pasatiempos = "$pasatiempos Deportes"


            if(nombre.isEmpty()){
                Toast.makeText(this, "Campo Nombre Vacio", Toast.LENGTH_LONG).show()
            }
            else if (correo.isEmpty() || "@" !in correo){
                Toast.makeText(this, "Campo Correo Invalido", Toast.LENGTH_LONG).show()
            }
            else if (contrasena.isEmpty()){
                Toast.makeText(this, "Campo Contraseña Vacio", Toast.LENGTH_LONG).show()
            }
            else if (repContra.isEmpty()){
                Toast.makeText(this, "Campo Rep Contraseña Vacio", Toast.LENGTH_LONG).show()
            }
            else if (fechanaci == "dd/MM/yy"){
                Toast.makeText(this, "Campo Fecha Vacio", Toast.LENGTH_LONG).show()
            }
            else {
                if (contrasena == repContra){
                    //Toast.makeText(this, "Registro Completado", Toast.LENGTH_LONG).show()
                    /*tv_result.text = "\nNombre: $nombre " +
                            "\nCorreo: $correo " +
                            "\nFecha de Nacimiento: $fecha " +
                            "\nCiudad de Nacimiento: $ciudadNacimiento " +
                            "\nGenero: $genero " +
                            "\nPasatiempos:$pasatiempos \n"*/
                    val intent = Intent()
                    intent.putExtra("usuario", et_usuario.text.toString())
                    intent.putExtra("contraseña", et_contrasena.text.toString())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else{
                    Toast.makeText(this, "Contraseñas diferentes", Toast.LENGTH_LONG).show()
                }
            }

        }

    }
}