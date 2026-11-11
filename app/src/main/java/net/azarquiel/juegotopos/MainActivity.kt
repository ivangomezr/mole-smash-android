package net.azarquiel.juegotopos

import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val NTOPOS = 10 //para declarar una constante
    }
    private var isGameover: Boolean = false
    private var time: Long = 0
    private lateinit var tvscore: TextView
    private var puntos: Int = 0
    private lateinit var random: Random
    private lateinit var cl : ConstraintLayout
    private val ivTopos = arrayOfNulls<ImageView>(NTOPOS)
    private lateinit var sp: SoundPool
    private var sonido: Int = 0
    private var sonidoStream: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        random = Random(System.currentTimeMillis())
        time = System.currentTimeMillis()
        cl = findViewById<ConstraintLayout>(R.id.cl)
        tvscore = findViewById<TextView>(R.id.tvscore)
        newGame()
    }

    private fun newGame() {
        puntos = 0
        time = System.currentTimeMillis()
        isGameover = false
        showScore()
        rellenarArrayTopos()
        sacaTopos()
    }

    private fun sacaTopos() {
        val ntopos = (1..NTOPOS/2).random(random)
        val vector = IntArray(10){i->i}         //en cada posicion del array tenemos el valor del indice
        vector.shuffle(random)
        val vtopos = vector.copyOfRange(0, ntopos)   //metodo para copiar un array a otro desde una posicion inicial y final dadas
        showTopos(vtopos)
        tapaTopos(vtopos)
    }

    private fun tapaTopos(vtopos: IntArray) {
        val time = (1..3).random(random) * 500
        GlobalScope.launch() {
            SystemClock.sleep(time.toLong())
            launch(Main) {
                for(n in vtopos) {
                    ivTopos[n]!!.setBackgroundResource(android.R.color.transparent)
                    ivTopos[n]!!.isEnabled = false
                }
                if (!isGameover)sacaTopos()
            }
        }
    }

    private fun showTopos(vtopos: IntArray) {
        for (n in vtopos) {
            val imageshow = ivTopos[n]
            val mor = (1..10).random(random) //10% de probabilidades de que salga un topo rojo con un numero del 1 al 10
            if (mor == 1) {                        // si sale el 1, topo rojo, sino marron
                showTopo(imageshow, "especial")
            } else {
                showTopo(imageshow, "normal")
            }
        }
    }

    private fun showTopo(imageshow: ImageView?, tipoAnim: String) {
        val id = resources.getIdentifier("animacion_$tipoAnim", "drawable", packageName)
        imageshow!!.setBackgroundResource(id)
        val imageshowAD = imageshow.background as AnimationDrawable
        imageshowAD.start()
        imageshow.isEnabled = true
        imageshow.tag = tipoAnim
    }

    private fun rellenarArrayTopos() {
        for (i in 0 until cl.childCount) {
            ivTopos[i] = cl.getChildAt(i) as ImageView
            ivTopos[i]!!.setOnClickListener(this)
            ivTopos[i]!!.isEnabled = false
        }
    }

    override fun onClick(v: View?) {
        val iv = v as ImageView
        val tag = iv.tag
        if (tag == "normal") puntos++
        else puntos += 10
        iv.isEnabled = false
        showScore()
        deathSound()
        val dead = if (tag=="normal") "tdead" else "tedead"  //si el tag es "normal", la variable vale "tdead"
        val id = resources.getIdentifier(dead, "drawable", packageName)
        iv.setBackgroundResource(id)

        if (puntos >= 20) gameOver()
    }

    private fun deathSound() {
        sp = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        sonido = sp.load(this, R.raw.sonidomuerte, 1)

        sonidoStream = sp.play(sonido, 1f, 1f, 1, -1, 1f)

        GlobalScope.launch() {
            SystemClock.sleep(1000)
            launch(Main) {
                sp.stop(sonidoStream)
            }
        }
    }

    private fun gameOver() {
        isGameover=true
        val segundos = (System.currentTimeMillis()-time)/1000
        AlertDialog.Builder(this)
        .setTitle("GameOver")
        .setMessage("Lo conseguiste en $segundos segundos")
        .setCancelable(false)
        .setPositiveButton("New game") { dialog, which ->
            newGame()
        }
        .setNegativeButton("Exit") { dialog, which ->
            finish()
        }
        .show()
    }

    private fun showScore() {
        tvscore.text = "$puntos puntos"
    }

}




