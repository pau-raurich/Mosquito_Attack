package cat.dam.pau.mosquito_attack;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public final int DEFAUL_TIME = 10;
    public int time = DEFAUL_TIME;
    public int score = 0;
    public int mosquitoSpeed = 1500;
    public int difficulty = 1;
    public Random r = new Random();

    LinearLayout ll_content;

    ArrayList <ImageView> mosquitoes = new ArrayList<>();

    AnimationDrawable mosquito_anim_list;
    AnimationDrawable smashed_anim_list;

    TextView tv_time;
    TextView tv_score;

    Button btn_start;


    //Temporitzador de temps
    CountDownTimer countDown = new CountDownTimer(time*1000, 1000) {
        public void onTick(long millisUntilFinished) {
            time--;
            millisUntilFinished = time;
            tv_time.setText("Time: " + time);

            if (time == 0) countDown.onFinish();

        }

        public void onFinish() {
            tv_time.setText("Time: "+0);
            lostGame();
        }
    };

    //Temporitzador de moviment del mosquit
    CountDownTimer mosquitoMovement =  new CountDownTimer(500000000, mosquitoSpeed) {
        public void onTick(long millisUntilFinished) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);



            for(ImageView m : mosquitoes) {
                params.leftMargin = r.nextInt(500);
                params.topMargin = r.nextInt(500);

                m.setLayoutParams(params);
            }
        }
        public void onFinish() {
            tv_time.setText("Time: "+0);
            lostGame();

        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_score = (TextView) findViewById(R.id.tv_score);

        btn_start = (Button) findViewById(R.id.btn_start);

        ll_content = (LinearLayout)findViewById(R.id.ll_content);

        tv_time.setText("Time: " + time);
        tv_score.setText("Score: " + score);

        mosquito_anim_list = new AnimationDrawable();
        smashed_anim_list = new AnimationDrawable();

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                countDown.start();//Inici de comptador
                btn_start.setVisibility(View.GONE); //Treiem el boto start
                initMosquito(); //Iniciem un mosquit

            }
        });
    }



    //Funció que inicia un mosquit o varís segons el nivell de dificultat en el que s'estigui
    public void initMosquito(){
        if(score % 5 != 0 || score == 0){
            createMosquito();

        }else{
            difficulty++;
            checkDifficulty();
            mosquitoSpeed = mosquitoSpeed-500;
        }
    }

    //Funció que crea un mosquit segons la dificultat en la que s'estigui
    public void checkDifficulty(){
        for (int i=0;i<difficulty;i++){
            createMosquito();
        }
    }

    //Funció que crea un mosquit, el seu listener i la seva posició
    public void createMosquito(){
        mosquitoes.add((ImageView) new ImageView(MainActivity.this));

        createOnClickListener( mosquitoes.get(mosquitoes.size()-1));

        mosquitoes.get(mosquitoes.size()-1).setBackgroundResource(R.drawable.mosquito_anim_list);
        mosquitoes.get(mosquitoes.size()-1).setVisibility(View.VISIBLE);

        mosquitoes.get(mosquitoes.size()-1).setX(r.nextInt(500));
        mosquitoes.get(mosquitoes.size()-1).setY(r.nextInt(500));

        ll_content.addView(mosquitoes.get(mosquitoes.size()-1));

        initMosquitoAnim();
        initMosquitoMovement();
    }

    //Funció que inicia l'animació del mosquit
    public void initMosquitoAnim(){
        mosquito_anim_list = (AnimationDrawable) mosquitoes.get(mosquitoes.size()-1).getBackground();
        mosquito_anim_list.start();
    }

    //Funció que inicia el moviment del mosquit
    public void initMosquitoMovement(){
        mosquitoMovement.start();
    }

    //Funció que mostra l'animació de quan s'aplasta un mosquit i suma +1 al comptador de temps
    public void smashedMosquito(ImageView mosquito){
        score++;
        tv_score.setText("Score: " + score);
        new CountDownTimer(800, 400) {
            public void onTick(long millisUntilFinished) {
                time += 2; //Sumem +2 perque es mostra una vegad pasa un segon
                countDown.cancel(); //Cancel·lem el comptador que hi ha per tal de que no hi hagi dos actius
                countDown.start(); //I el tornem a iniciar amb el nou temps establert


                //Carreguem l'animació de la sang
                mosquito.setBackgroundResource(R.drawable.smashed_anim_list);
                smashed_anim_list = (AnimationDrawable) mosquito.getBackground();
                smashed_anim_list.start();
                

                mosquito.setEnabled(false); //Deshabilitem que es pugui fer click a l'imatge
            }

            public void onFinish() {
                mosquito.setVisibility(View.GONE);
                initMosquito();
            }
        }.start();
    }

    //Funció que crea el listener dels mosquits
    public void createOnClickListener(ImageView m){
        m.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                smashedMosquito(m); //Començem l'animació del mosquit xafat
                mosquitoMovement.cancel(); //I parem el moviment d'aquest
            }
        });
    }

    //Funció que mostra el contingut de quan es perd la partida i reinicia totes les variables
    public void lostGame(){
        time = DEFAUL_TIME;
        countDown.cancel();
        lostGameDialog();
        score = 0;
        tv_score.setText("Score: "+score);

        for (ImageView m:mosquitoes) {
            m.setVisibility(View.GONE);
        }

        btn_start.setVisibility(View.VISIBLE);
    }

    //Funció que mostra el dialog quan es perd
    public void lostGameDialog(){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("HAS PERDUT!!");
        dialogo1.setMessage("La teva puntuació ha sigut de: "+score);
        dialogo1.setCancelable(false); //Fem que no es pugui sortir presionant fora del cuadre

        //En cas de presionar el boto positiu
        dialogo1.setPositiveButton("Sortir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                finish();
            }
        });

        //En cas de presionar del boto negatiu
        dialogo1.setNegativeButton("Reiniciar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                closeOptionsMenu(); //Tancament del cuadre d'opcions
            }
        });
        dialogo1.show();
    }
}