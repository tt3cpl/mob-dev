package io.github.mobdev

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val but_0 = findViewById<TextView>(R.id.but_0)
        but_0.setOnClickListener { setTextFields("0") }
        val but_1 = findViewById<TextView>(R.id.but_1)
        but_1.setOnClickListener { setTextFields("1") }
        val but_2 = findViewById<TextView>(R.id.but_2)
        but_2.setOnClickListener { setTextFields("2") }
        val but_3 = findViewById<TextView>(R.id.but_3)
        but_3.setOnClickListener { setTextFields("3") }
        val but_4 = findViewById<TextView>(R.id.but_4)
        but_4.setOnClickListener { setTextFields("4") }
        val but_5 = findViewById<TextView>(R.id.but_5)
        but_5.setOnClickListener { setTextFields("5") }
        val but_6 = findViewById<TextView>(R.id.but_6)
        but_6.setOnClickListener { setTextFields("6") }
        val but_7 = findViewById<TextView>(R.id.but_7)
        but_7.setOnClickListener { setTextFields("7") }
        val but_8 = findViewById<TextView>(R.id.but_8)
        but_8.setOnClickListener { setTextFields("8") }
        val but_9 = findViewById<TextView>(R.id.but_9)
        but_9.setOnClickListener { setTextFields("9") }
        val but_minus = findViewById<TextView>(R.id.but_minus)
        but_minus.setOnClickListener { setTextFields("-") }
        val but_plus = findViewById<TextView>(R.id.but_plus)
        but_plus.setOnClickListener { setTextFields("+") }
        val but_umn = findViewById<TextView>(R.id.but_umn)
        but_umn.setOnClickListener { setTextFields("*") }
        val but_delen = findViewById<TextView>(R.id.but_delen)
        but_delen.setOnClickListener { setTextFields("/") }
        val but_open = findViewById<TextView>(R.id.but_open)
        but_open.setOnClickListener { setTextFields("(") }
        val but_close = findViewById<TextView>(R.id.but_close)
        but_close.setOnClickListener { setTextFields(")") }
        val but_zap = findViewById<TextView>(R.id.but_zap)
        but_zap.setOnClickListener { setTextFields(".") }


        val but_ac = findViewById<TextView>(R.id.but_ac)
        but_ac.setOnClickListener {
            val math_operation = findViewById<TextView>(R.id.math_operation)
            val resultText = findViewById<TextView>(R.id.result_text)
            math_operation.text = ""
            resultText.text = ""
        }

        val but_back = findViewById<TextView>(R.id.but_back)
        but_back.setOnClickListener {
            val math_operation = findViewById<TextView>(R.id.math_operation)
            val str = math_operation.text.toString()
            if (str.isNotEmpty())
                math_operation.text = str.substring(0, str.length - 1)
            val resultText = findViewById<TextView>(R.id.result_text)
            resultText.text = ""
        }

        val but_result = findViewById<TextView>(R.id.but_result)
        but_result.setOnClickListener {
            try {
                val math_operation = findViewById<TextView>(R.id.math_operation)
                val ex = ExpressionBuilder(math_operation.text.toString()).build()
                val result = ex.evaluate()

                val result_text = findViewById<TextView>(R.id.result_text)
                val longRes = result.toLong()
                if (result == longRes.toDouble())
                    result_text.text = longRes.toString()
                else
                    result_text.text = result.toString()
            } catch (e: Exception) {
                Log.d("Ошибка", "сообщение: ${e.message}")
            }
        }
    }

    fun setTextFields(str: String) {
        val result_text = findViewById<TextView>(R.id.result_text)
        val math_operation = findViewById<TextView>(R.id.math_operation)
        if (result_text.text != "") {
            math_operation.text = result_text.text
            result_text.text = ""
        }
        math_operation.append(str)

    }
}