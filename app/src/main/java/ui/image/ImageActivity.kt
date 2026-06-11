package ui.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import app.mobdev.R
import coil.load

// экран просмотра изображения
class ImageActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() //скрываем ActionBar
        setContentView(R.layout.activity_image) // подключаем layout activity_image.xml
        
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ?: return // получаем ссылку на изображение из Intent
        
        val imageView = findViewById<ImageView>(R.id.imageView) // находим ImageView
        imageView.load(imageUrl) // загружаем картинку по URL
        
        findViewById<FrameLayout>(android.R.id.content).setOnClickListener { // Если нажали в любое место экрана закрывает Activity
            finish()
        }
    }
    
    companion object {
        const val EXTRA_IMAGE_URL = "image_url" // ключ для передачи URL картинки через Intent
        
        fun newIntent(context: Context, imageUrl: String): Intent {
            return Intent(context, ImageActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
        }
    }
}
