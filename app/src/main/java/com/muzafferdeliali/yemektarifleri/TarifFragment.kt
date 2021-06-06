package com.muzafferdeliali.yemektarifleri

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
//import android.renderscript.ScriptGroup
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
//import android.widget.Button
//import androidx.constraintlayout.widget.ConstraintLayout
//import com.muzafferdeliali.yemektarifleri.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

//import com.muzafferdeliali.yemektarifleri.kaydet as kaydet1

class TarifFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            kaydet(it)

        }

        imageView.setOnClickListener {
            gorselSec(it)
        }   
            arguments?.let {
                var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
                if(gelenBilgi.equals("menudengeldim")){
                    //yeni bir yemek eklemeye geldi
                    YemekAdi.setText("")
                    YemekMalzemeleri.setText("")
                    button.visibility = View.VISIBLE

                    val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.ekle)
                    imageView.setImageBitmap(gorselSecmeArkaPlani)
                } else{
                    //daha önce oluşturulan yemeği görmeye geldi
                    button.visibility = View.INVISIBLE

                    val secilenId = TarifFragmentArgs.fromBundle(it).id
                    context?.let {
                        try {

                            val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                            val cursor = db.rawQuery("SELECT * FROM Yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                            val YemekAdi = cursor.getColumnIndex("YemekAdi")
                            val YemekMalzemeleri = cursor.getColumnIndex("YemekMalzemeleri")
                            val Gorsel = cursor.getColumnIndex("Gorsel")

                            while (cursor.moveToNext()){
                                YemekAdi.setText(cursor.getString(YemekAdi))
                                YemekMalzemeleri.setText(cursor.getString(YemekMalzemeleri))

                                val byteDizisi =cursor.getBlob(Gorsel)
                                val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                                imageView.setImageBitmap(bitmap)
                            }

                            cursor.close()
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    fun kaydet  (view: View){
        //SQLite'a Kaydetme
        val YemekAdi = YemekAdi.text.toString()
        val YemekMalzemeleri = YemekMalzemeleri.text.toString()

        if(secilenBitmap != null){

            val kucukBitmap = kucukBitMapOlustur(secilenBitmap!!,400)
// Görselleri byte a dönüştürüp veri tabanına kayıt ediyoruz
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
            context?.let {
                val database = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS Yemekler(id INTEGER PRIMARY KEY, YemekAdi VARCHAR,YemekMalzemeleri VARCHAR, Gorsel BLOB) ")
                val sqlString = "INSERT INTO Yemekler(YemekAdi,YemekMalzemeleri,Gorsel)VALUES (?,?,?)"

                val statement = database.compileStatement(sqlString)
                statement.bindString(1,YemekAdi)
                statement.bindString(2,YemekMalzemeleri)
                statement.bindBlob(3,byteDizisi)
                statement.execute()
            }

            }catch (e: Exception){
                    e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }
    }


    fun gorselSec(view: View){

        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //izin verilmedi , izin istenmesi gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else {
                //zaten izin verilmişş tekrardan izin verilemesi gerekmiyor
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {

            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //izin başarıyla alındı

                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
// Burada tekrardan result kontrol etme sebebimiz kullanıcı izin vermekten vaz geçip geçmediğini kontrol etmektir.
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            secilenGorsel = data.data
// Try and catch kullanılmasının sebebi olası hataları en aza indirmektir
            try {

                context?.let {
                    if (secilenGorsel != null) {
                        if (Build.VERSION.SDK_INT >=28) {
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            imageView.setImageBitmap(secilenBitmap)
                        }
                }
                }

            }catch (e:Exception) {
                    e.printStackTrace()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
            //BitMap i küçültme sebebimiz gelen veri girişini start hale getirmek ve optimizasyonu sağlamak
            fun kucukBitMapOlustur(KullanicininSectigiBitmap :Bitmap, maximumBoyut :Int ):Bitmap{

                var width  = KullanicininSectigiBitmap.width
                var height = KullanicininSectigiBitmap.height

                var BitMapOran : Double = width.toDouble() / height.toDouble()

                if (BitMapOran > 1){
                    //Görsel Yatay
                    width = maximumBoyut
                    val kisaltilmisHeight = width / BitMapOran
                    height = kisaltilmisHeight.toInt()

                }else{
                    //Görsel Dikey
                    height = maximumBoyut
                    val kisaltilmisWidth = height * BitMapOran
                    width = kisaltilmisWidth.toInt()

                }

                return Bitmap.createScaledBitmap(KullanicininSectigiBitmap,width,height,true)
}
}


