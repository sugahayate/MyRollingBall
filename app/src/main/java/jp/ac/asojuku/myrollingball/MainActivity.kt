package jp.ac.asojuku.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    SensorEventListener,//各種センサーの反応をOSから受け取るためのインターフェース
    SurfaceHolder.Callback{//サーフェイスViewを実装するための窓口Holderのコールバックインターフェース
//インスタンスプロパティ変数
private var surfaceWidth:Int = 0//サーフェイスビューの幅
    private var surfaceHeight:Int = 0;//サーフェイスビューの高さ

    private val radius = 40.0f;//ボールの半径
    private val coef = 600.0f;//ボールの移動量を計算するための係数

    private var ballX:Float = 0f;//ボールのｘ座標
    private var ballY:Float = 0f;//ボールのy座標
    private var vx:Float = 0f;//ボールのｘ座標の重力加速度
    private var vy:Float = 0f;//ボールのｙ座標の重力加速度
    private var time:Long = 0L;//前回の時間を記録する変数


    //画面生成時のイベントコールバックメソッド
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_main)//画面レイアウトを設定
        //サーフェイスホルダーをサーフェイスView部品から取得
        val holder = surfaceView.holder;
        //サーフェイスホルダーのコールバック虹クラスへの通知を追加
        // サーフェスホルダーのコールバックに自クラスを追加
        holder.addCallback(this);
        // 画面の縦横指定をアプリから指定してロック(縦方向に指定)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    }

    //画面の表示・再表示のイベントコールバックメソッド
    override fun onResume() {
        super.onResume()
        // 初期位置へ
        fun reset(x : Float, y : Float, vx : Float, vy :Float ) {
            ballX = x
            ballY = y
            this.vx = vx
            this.vy = vy
            cat.setImageResource(R.drawable.cat)
            fight.setText("がんばれ！")
        }
        reset_btn.setOnClickListener{
            reset(100f,100f,0f,0f)
        }

    }
    //画面が非表示になるときに呼ばれるコールバックメソッド
    override fun onPause() {
        super.onPause()
        //センサーマネージャーのインスタンスを取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャーから登録した自クラス（画面）への通知を解除(OFF)
        sensorManager.unregisterListener(this);
    }

    //SensorEventListenerの必須のoverrideメソッド（センサーの精度が変わるとコールバックされる）
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //今回は何もしない
    }

    //SensorEventListenerの必須のoverrideメソッド(センサーの取得値が変わるとコールバックされる)
    override fun onSensorChanged(event: SensorEvent?) {
        //eventの中身がnullなら何もせずにreturn
        if (event == null) {
            return;
        }

        //センサーが変わったときに、ボールを描画するアメンお情報を計算する
        //一番最初のセンサー検知の時の、初期時間を取得
        if (time == 0L) {
            time = System.currentTimeMillis()
        }//最初は現在のミリ秒システム時刻を設定
        //eventのセンサー種別が加速度センサーだったら、以下実行
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //センサーが取得した値（左右の変化:x,上下の変化：y,）を変数に代入
            val x = event.values[0] * -1;//横左右の値
            val y = event.values[1];

            //前回の時間（time）からの経過時間を計算（今の時間ー前回の時間＝経過時間）
            var t = ((System.currentTimeMillis()) - time).toFloat();//計算結果はFloat型にしておく
            //timeに今の時間を「前の時間」として保存し直す
            time = System.currentTimeMillis();
            t /= 1000.0f;//ミリ秒単位を秒単位に直すたえに１０００でわる（5327ミリ秒＝5.327秒）

            //移動距離を計算（ボールの座標をどれくらい動かすか）
            val dx = (vx * t) + (x * t * t) / 2.0f;//xの移動すべき距離
            val dy = (vy * t) + (y * t * t) / 2.0f;//yの移動すべき距離
            this.ballX += (dx * coef);
            this.ballY += (dy * coef);
            //
            this.vx += (x * t);
            this.vy += (x * t);

            //画面の端に来たら跳ね返る処理
            // 左右について
            if ((ballX - radius) < 0 && vx < 0) {
                // 左にぶつかった時
                vx = -vx / 1.5f;
                ballX = radius;
            } else if ((ballX + radius) > surfaceWidth && vx > 0) {
                // 右にぶつかった時
                vx = -vx / 1.5f;
                ballX = (surfaceWidth - radius);
            }
            // 上下について
            if ((ballY - radius) < 0 && vy < 0) {
                // 下にぶつかった時
                vy = -vy / 1.5f;
                ballY = radius;
            } else if ((ballY + radius) > surfaceHeight && vy > 0) {
                // 上にぶつかった時
                vy = -vy / 1.5f;
                ballY = surfaceHeight - radius;
            }

            //キャンパスに描画する命令
            this.drawCanvas();

            fun zero( vx : Float, vy :Float ){
                this.vx=vx;
                this.vy=vy;
            }
            //オブジェクトに対する当たり判定と処理
            //外れ
            if (ballX >= 0f && ballX <= 400f &&
                        ballY >= 500f && ballY <= 600f
            ) {
                zero(0f,0f);
                cat.setImageResource(R.drawable.donmai)
                fight.setText("どんまい・・・")
            }
            //外れ
            if (ballX >= 500f && ballX <= 700f &&
                ballY >= 100f && ballY <= 300f
            ) {
                zero(0f,0f);
                cat.setImageResource(R.drawable.donmai)
                fight.setText("どんまい・・・")
            }
            //外れ
            if (ballX >= 650f && ballX <= 750f &&
                ballY >= 650f && ballY <= 900f
            ) {
                zero(0f,0f);
                cat.setImageResource(R.drawable.donmai)
                fight.setText("どんまい・・・")
            }
            //外れ
            if (ballX >= 0f && ballX <= 400f &&
                ballY >= 850f && ballY <= 950f
            ) {
                zero(0f,0f);
                cat.setImageResource(R.drawable.donmai)
                fight.setText("どんまい・・・")
            }
            //あたり
            if (ballX >= 200f && ballX <= 300f &&
                ballY >= 670f && ballY <= 770f
            ) {
                zero(0f,0f);
                cat.setImageResource(R.drawable.sugoi)
                fight.setText("すごい！")
            }




        }


        //センサーの値が変わった時の処理をここに書く
        /*Log.d("TAG01","センサーの値が変わりました");
        //引数（イベント）の中身が何もなかったら何もせずに終了
        if(event == null){return;}
        //加速度センサーのイベントが判定
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //ログに表示するための文字列を組み立てる
            var str:String = "xの値：${event.values[0].toString()}"+
                    "y方向の値:${event.values[1].toString()}"+
                    "z方向の値:${event.values[2].toString()}";
            //でバックログに出力
            Log.d("加速度センサー",str);


        }*/


    }


    //サーフェイスが更新さえれた時のイベント時反応して呼ばれるコーリバックメソッド
    override fun surfaceChanged(holder: SurfaceHolder?,
                                format: Int,
                                width: Int,
                                height: Int) {
        //サーフェイスが変化するたびに幅と高さを設定
        // サーフェスの幅と高さをプロパティに保存しておく
        surfaceWidth = width;
        surfaceHeight = height;
        // ボールの初期位置を保存しておく
        ballX = 100f;
        ballY = 100f;

    }
    //サーフェイスが破棄された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //加速度センサーの登録を解除する流れ
        // センサーマネージャを取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        // センサーマネージャを通じてOSからリスナー（自分自身）を登録解除
        sensorManager.unregisterListener(this);
    }
    //サーフェイスが生成された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceCreated(holder: SurfaceHolder?) {
        //センサーマネージャーのインスタンスをOSから取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャーから加速度センサー(Accelermeter)を指定してそのインスタンスを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //センサーのリスナーに登録して加速度センサーの監視を開始(ONにする)
        sensorManager.registerListener(
            this,//イベントリスナー機能を持つインスタンス（今回は画面クラス。ここに通知してもらう）
            accSensor,//監視するセンサーのインスタンス（今回は加速度センサー）
            SensorManager.SENSOR_DELAY_GAME//センサーの更新通知と頻度
        )
    }

    //サーフェイスのキャンパスに描画する処理をまとめたメソッド
    private fun drawCanvas(){
        //キャンバスをロックして取得する
        val canvas = surfaceView.holder.lockCanvas();
        //キャンバスに背景の色を塗る
        canvas.drawColor(Color.GREEN);//グリーン
        //キャンバスに円を書いてボールにする
        canvas.drawCircle(
            this.ballX,//ボールのX座標
            this.ballY,//ボールのY座標
            this.radius,//ボールの半径
            Paint().apply{
                this.color = Color.RED//色を赤にする
            }
        )

        canvas.drawRect(0f, 500f, 400f, 600f,//外れ
            Paint().apply {
                this.color = Color.BLACK;
            }
        )

        canvas.drawCircle(
            600f,//ボールのX座標
            200f,//ボールのY座標
            100f,//ボールの半径
            Paint().apply{
                this.color = Color.BLACK//外れ
            }
        )

        canvas.drawRect(650f, 650f, 750f, 900f,//外れ
            Paint().apply {
                this.color = Color.BLACK;
            }
        )

        canvas.drawRect(0f, 850f, 400f, 950f,//外れ
            Paint().apply {
                this.color = Color.BLACK;
            }
        )

        canvas.drawRect(200f, 670f, 300f, 770f,//ゴール
            Paint().apply {
                this.color = Color.RED;
            }
        )






        //キャンバスをロック解除してキャンバスを描画
        surfaceView.holder.unlockCanvasAndPost(canvas);


    }
}
