import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.embed.swing.JFXPanel;
import javax.swing.JFrame;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.*;
import sun.audio.*;
public class game extends JFrame{
   
public static void main(String[] ar) {
   game_Frame fms = new game_Frame();
   try{         
      File theFile = new File("Terran.wav");
   
      FileInputStream fis = new FileInputStream(theFile); 
      AudioStream as = new AudioStream(fis);
      AudioPlayer.player.start(as);
      //배경음악 삽입      
   }catch(Exception ex) {}

   }
}
   

   class game_Frame extends JFrame implements KeyListener, Runnable {
      
      int f_width; 
      int f_height; 
      int x, y; //플레이어캐릭터의현재좌표값을받을변수

      int[] cx = { 0, 0, 0 }; // 배경스크롤속도제어용변수
      int bx = 0; // 전체배경스크롤용변수

      boolean KeyUp = false; //키보드키값을받을변수
      boolean KeyDown = false;
      boolean KeyLeft = false;
      boolean KeyRight = false;
      boolean KeySpace = false;

      int cnt; //무한루프횟수를카운트할변수

      int player_Speed;// 유저의캐릭터가움직이는속도를조절할변수
      int missile_Speed;// 미사일이날라가는속도조절할변수
      int missile_Speed2;
      int fire_Speed; // 미사일연사속도조절변수
      int enemy_speed; // 적이동속도설정
      int player_Status = 0;// 유저캐릭터상태체크변수0 : 평상시, 1: 미사일발사, 2: 충돌
      int game_Score; // 게임점수계산
      int player_Hitpoint; // 플레이어캐릭터의체력

      Thread th; //스레드생성

      Image[] Player_img;// 플레이어이미지
      Image BackGround_img; // 배경화면
      Image[] Cloud_img; // 움직이는배경용
      Image[] Explo_img; // 폭발이펙트용

      Image Missile_img; //플레이어미사일이미지생성
      Image Enemy_img; // 적이미지생성
      Image Missile2_img;// 적미사일이미지추가생성

      ArrayList Missile_List = new ArrayList(); //다수의미사일을관리하기위한배열
      ArrayList Enemy_List = new ArrayList();//다수의적을관리하기위한배열
      ArrayList Explosion_List = new ArrayList();// 다수의폭발이펙트를처리하기위한배열

      Image buffImage; 
      Graphics buffg; 

      Missile ms; //미사일클래스접근키
      Enemy en; // 에너미클래스접근키

      Explosion ex; // 폭발이펙트용클래스접근키

/*
* 프레임생성부분
*/

      game_Frame() {//화면에보여질프레임생성메소드
         init();
         start();
         

         setTitle("슈팅게임만들기");//프레임타이틀설정
         setSize(f_width, f_height);//프레임크기설정

         Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

         int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
         int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
         //프레임을모니터화면정중앙에배치

         setLocation(f_xpos, f_ypos);
         setResizable(false); 
         setVisible(true);
         
      }
      

/*
* 기본설정부분
*/
      
      public void init() { //기본적인게임설정을 관리
         x = 100; //최초플레이어시작x좌표
         y = 100; //최초플레이어시작y좌표
         f_width = 1200; //프레임넓이
         f_height = 600; //프레임높이

         Missile_img = new ImageIcon("Missile.png").getImage();
         //플레이어미사일이미지파일받아들이기
         Missile2_img = new ImageIcon("Missile2.png").getImage();
         //적미사일이미지파일받아들이기
         Enemy_img = new ImageIcon("enemy_1.png").getImage();
         //적이미지파일받아들이기

         Player_img = new Image[5];
         for (int i = 0; i < Player_img.length ; ++i) {
            Player_img[i] = 
            new ImageIcon("f15k_" + i + ".png").getImage();
         }
         // 플레이어 애니메이션표현을 위해 파일 이름을 나눠서 배열로 담는다

         BackGround_img = new ImageIcon("background.png").getImage();
         // 전체배경화면이미지를받습니다.

         Cloud_img = new Image[3];
         for (int i = 0; i < Cloud_img.length ; ++i) {
            Cloud_img[i] = new ImageIcon("cloud_" + i + ".png").getImage();
         }
         // 구름을 3개 동시에 그리는데 편의상 배열로 3개를 동시에받는다.

         Explo_img = new Image[3];
         for (int i = 0; i < Explo_img.length ; ++i) {
            Explo_img[i] = 
            new ImageIcon("explo_" + i + ".png").getImage();
         }
         // 폭발애니메이션표현을위해
         // 파일이름을넘버마다나눠배열로담는다.

         game_Score = 0;// 게임스코어초기화
         player_Hitpoint = 3;// 최초플레이어체력

         player_Speed = 5; // 유저캐릭터움직이는속도설정
         missile_Speed = 11; // 미사일움직임속도설정
         missile_Speed2 = 22;
         fire_Speed = 15; // 미사일연사속도설정
         enemy_speed = 3;// 적이날라오는속도설정
      }

      public void start() { //기본적으로실행하는메소드
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         addKeyListener(this);

         th = new Thread(this);
         //새로운스레드를생성
         th.start();
         //스레드시작      
      }
      
/*
* 스레드
*/
      public void run() {//스레드실행부분
         try {
            while (true) { // 무한루프돌리기

               KeyProcess();
               EnemyProcess();
               MissileProcess();
               ExplosionProcess();
               //각종처리메소드실행

               repaint();

               Thread.sleep(20);

               cnt++;
               
            }
         } catch (Exception e) {}
      }
      

/*
* 메인처리메소드
*/
      public void MissileProcess() {//미사일관련처리메소드
         if (KeySpace) { //키보드스페이스키입력여부확인
            player_Status = 1; //플레이어상태를1로변경

            if ((cnt % fire_Speed) == 0) { 
               //루프카운터의값에서fire_Speed에서설정한값만큼
               //나눈값의나머지가0인지여부를확인

               ms = new Missile(x + 150, y + 30, 0, missile_Speed, 0);
               // 기본적인 오른쪽 직행 미사일
               // 각도값은 0이 기본 값입니다.
               //왼쪽부터 미사일 x좌표, y좌표, 미사일 진행방향, 미사일 속도, 미사일 종류
               //미사일 종류 0 : 플레이어가 발사하는 미사일, 1 : 적이 발사하는 미사일
               Missile_List.add(ms);

               ms = new Missile(x + 150, y + 30, 330, missile_Speed, 0);
               // 위쪽 대각선으로 날라갈 미사일
               Missile_List.add(ms);

               ms = new Missile(x + 150, y + 30, 30, missile_Speed, 0);
               // 아래쪽 대각선으로 날라갈 미사일
               Missile_List.add(ms);

            }
         }

         for (int i = 0; i < Missile_List.size(); ++i) {
            
            ms = (Missile) Missile_List.get(i);
            //배열에존재하는 미사일의객체를 받아온다.
            ms.move();
            //해당미사일을움직이게만든다
            if (ms.x > f_width - 20 || ms.x < 0 || ms.y < 0 || ms.y > f_height ) {
               // 해당미사일이화면밖으로나갔는가여부를확인
               Missile_List.remove(i);
               //화면끝까지도달한미사일삭제
            }

            if ( Crash(x, y, ms.x, ms.y, Player_img[0], Missile_img) && ms.who == 1 ) {
               //적이 발사한 미사일이 플레이어와 충돌
               player_Hitpoint --;
              
               //플레이어 체력 포인트를 1삭감
               ex = new Explosion(x, y, 1);
               //플레이어 자리에 충돌용 폭발 이펙트 객체생성
               Explosion_List.add(ex);
               //생성한 객체를 배열로저장
               Missile_List.remove(i);
               //해당되는 적미사일 삭제
            }

            for (int j = 0; j < Enemy_List.size(); ++j) {
               en = (Enemy) Enemy_List.get(j);
               //배열에 존재하는 적의 객체를 받아온다
               if (Crash(ms.x, ms.y, en.x, en.y, Missile_img, Enemy_img) && ms.who == 0) {
                  //플레이어미사일과적충돌판정
                  
                  Missile_List.remove(i);
                  //충돌한미사일 삭제
                  Enemy_List.remove(j);
                  //충돌한 적 삭제

                  game_Score += 10; // 게임점수를+10점.

                  ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2,
                        en.y + Enemy_img.getHeight(null) / 2, 0);
                  // 적이 위치해 있는 곳의 중심 좌표x,y 값과
                  // 폭발설정값- 0 : 폭발, 1 : 단순피격

                  Explosion_List.add(ex);
                  // 충돌판정으로 사라진 적의 위치에
                  // 이펙트를 추가한다.

      
      try{
          File file = new File( "mfile.wav" );
          FileInputStream fs = new FileInputStream(file); 
          AudioStream as = new AudioStream(fs);
          AudioPlayer.player.start(as);
         }catch(Exception ex){
             System.out.println( "sound exception" );
         } 

               }
            }
         }
      }
      //미사일이 적과 충돌시 나타나는 소리
      public void Sound(String file, boolean Loop) {
         Clip clip;
         try {
            AudioInputStream ais = AudioSystem.getAudioInputStream
                  (new BufferedInputStream(new FileInputStream(file)));
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            if(Loop) clip.loop(-1);
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      public void EnemyProcess() {//적의행동관련처리메소드

         for (int i = 0; i < Enemy_List.size(); ++i) {
            en = (Enemy) (Enemy_List.get(i));
            //해당 배열의 적 객체를 받아온다.
            en.move();
            //해당 적 움직이기
            if (en.x < -200) { // 적이x 좌표 왼쪽 화면 끝까지 도달여부확인
               Enemy_List.remove(i);
               //화면 끝까지 도달한 적 삭제
            }

            if ( cnt % 50 == 0){
               ms = new Missile (en.x, en.y + 25, 180, missile_Speed2, 1);
               //확인된 해당 적의 위치에 미사일생성
               //왼쪽부터 미사일 x좌표, y좌표, 미사일 진행방향, 미사일속도, 미사일종류
               //미사일종류 0 : 플레이어가발사하는미사일, 1 : 적이발사하는미사일
               Missile_List.add(ms);
               //생성된 미사일을 객체로 배열에 추가
            }


            if (Crash(x, y, en.x, en.y, Player_img[0], Enemy_img)) {
               // 플레이어와 적의 충돌을 판정

               player_Hitpoint--; // 플레이어 체력을 1 깍습니다.
               Enemy_List.remove(i); // 적을 제거합니다.
               game_Score += 10;
               // 제거된 적으로 게임스코어를 10 증가시킵니다.

               ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2, en.y
                     + Enemy_img.getHeight(null) / 2, 0);
               // 적이 위치해 있는 곳의 중심좌표 x,y 값
               // 폭발설정값- 0 : 폭발, 1 : 단순피격

               Explosion_List.add(ex);
               // 제거된 적 위치에 폭발 이펙트를 추가합니다.

               ex = new Explosion(x, y, 1);
               // 적이 위치해 있는 곳의 중심좌표 x,y 값과
               // 폭발 설정을 받은 값( 0 또는1 )을 받습니다.
               // 폭발설정 값- 0 : 폭발, 1 : 단순피격

               Explosion_List.add(ex);
               // 충돌시 플레이어의 위치에 충돌용 이펙트를 추가.

            }
         }
         if (cnt % 50 == 0) {
            en = new Enemy(f_width + 100, 100, 8);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 200, 10);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 300, 21);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 400, 18);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 500,16);
            Enemy_List.add(en);
            // 적 움직임 속도를 추가로 받아 적을 생성한다.

         }
      }

      public void ExplosionProcess() {
         // 폭발 이펙트 처리용 메소드

         for (int i = 0; i < Explosion_List.size(); ++i) {
            ex = (Explosion) Explosion_List.get(i);
            ex.effect();
            // 이펙트애니메이션을나타내기위해
            // 이펙트처리추가가발생하면해당메소드를호출.

         }
      }

      public boolean Crash(int x1, int y1, int x2, int y2, Image img1, Image img2) {

         boolean check = false;

         if (Math.abs((x1 + img1.getWidth(null) / 2)
               - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2 + img1
                     .getWidth(null) / 2)
               && Math.abs((y1 + img1.getHeight(null) / 2)
                     - (y2 + img2.getHeight(null) / 2)) < (img2
                           .getHeight(null) / 2 + img1.getHeight(null) / 2)) {
            check = true;
         } else {
            check = false;
         }

         return check;

      }

/*
* 이미지그리는부분
*/

      public void paint(Graphics g) {
         buffImage = createImage(f_width, f_height);
            buffg = buffImage.getGraphics();
            
            update(g);
      }

      public void update(Graphics g) {

         Draw_Background(); 
         Draw_Player();
         Draw_Enemy();
         Draw_Missile();
         Draw_Explosion();
         Draw_StatusText();
      

         g.drawImage(buffImage, 0, 0, this);
      }

   public void Draw_Background() {
   // 배경이미지를그리는부분입니다.
   
      buffg.clearRect(0, 0, f_width, f_height);
   
      if (bx > -3500) {
   
         buffg.drawImage(BackGround_img, bx, 0, this);
         bx -= 1;
         //배경이미지의x좌표는
         // 계속좌측으로이동한다. 
   
   } else {
      bx = 0;
   }
   
   for (int i = 0; i < cx.length ; ++i) {
   
   if (cx[i] < 1400) {
      cx[i] += 5 + i * 3;
   } else {
      cx[i] = 0;
   }
   
   buffg.drawImage(Cloud_img[i], 1200 - cx[i], 50 + i * 200, this);
   // 3개의 구름이미지를 각기 다른 속도값으로 좌측으로 움직임.
   }
   }
   
   public void Draw_Player() {
   //플레이어캐릭터를그리는부분
   
   switch (player_Status) {
   
   case 0: // 평상시
   if ((cnt / 5 % 2) == 0) {
   buffg.drawImage(Player_img[1], x, y, this);
   
   } else {
   buffg.drawImage(Player_img[2], x, y, this);
   }
   
   
   break;
   
   case 1: // 미사일발사
   if ((cnt / 5 % 2) == 0) {
   buffg.drawImage(Player_img[3], x, y, this);
   } else {
   buffg.drawImage(Player_img[4], x, y, this);
   }
   
   player_Status = 0;
   
   break;
   case 2: // 충돌
   break;
   
   }
   
   }

   public void Draw_Missile() {
      //미사일이미지를그리는부분
      for (int i = 0; i < Missile_List.size(); ++i) {
         //미사일배열에값이존재하면
         ms = (Missile) (Missile_List.get(i));
         if ( ms.who == 0 )buffg.drawImage(Missile_img, ms.x, ms.y, this);
         //플레이어가 발사한 이미지를 그린다.
         if ( ms.who == 1 )buffg.drawImage(Missile2_img, ms.x, ms.y, this);
         //적이 발사한 이미지를 그린다.
      }
   }

   public void Draw_Enemy() {
      //적이미지를 그리는 부분
      for (int i = 0; i < Enemy_List.size(); ++i) {
         en = (Enemy) (Enemy_List.get(i));
         buffg.drawImage(Enemy_img, en.x, en.y, this);
      }
   }

   public void Draw_Explosion() {
      // 폭발이펙트를 그리는 부분입니다.

      for (int i = 0; i < Explosion_List.size(); ++i) {
         ex = (Explosion) Explosion_List.get(i);
         // 폭발이펙트의 존재유무를 체크

         if (ex.damage == 0) {
            // 설정값이0 이면 폭발용 이미지 그리기

            if (ex.ex_cnt < 7) {
               buffg.drawImage(Explo_img[0],
                     ex.x - Explo_img[0].getWidth(null) / 2, ex.y
                     - Explo_img[0].getHeight(null) / 2, this);
            } else if (ex.ex_cnt < 14) {
               buffg.drawImage(Explo_img[1],
                     ex.x - Explo_img[1].getWidth(null) / 2, ex.y
                     - Explo_img[1].getHeight(null) / 2, this);
            } else if (ex.ex_cnt < 21) {
               buffg.drawImage(Explo_img[2],
                     ex.x - Explo_img[2].getWidth(null) / 2, ex.y
                     - Explo_img[2].getHeight(null) / 2, this);
            } else if (ex.ex_cnt > 21) {
               Explosion_List.remove(i);
               ex.ex_cnt = 0;
            
            }
         } else {
            if (ex.ex_cnt < 7) {
               buffg.drawImage(Explo_img[0], ex.x + 120, ex.y + 15, this);
            } else if (ex.ex_cnt < 14) {
               buffg.drawImage(Explo_img[1], ex.x + 60, ex.y + 5, this);
            } else if (ex.ex_cnt < 21) {
               buffg.drawImage(Explo_img[0], ex.x + 5, ex.y + 10, this);
            } else if (ex.ex_cnt > 21) {
               Explosion_List.remove(i);
               ex.ex_cnt = 0;
               
            }
         }
      }
   }

   public void Draw_StatusText() { 

      buffg.setFont(new Font("Defualt", Font.BOLD, 20));
      // 폰트설정을합니다. 기본폰트, 굵게, 사이즈20

      buffg.drawString("SCORE : " + game_Score, 1000, 70);
      // 좌표x : 1000, y : 70에스코어를표시합니다.

      buffg.drawString("HitPoint : " + player_Hitpoint, 1000, 90);
      // 좌표x : 1000, y : 90에플레이어체력을표시합니다.

      buffg.drawString("Missile Count : " + Missile_List.size(), 1000, 110);
      // 좌표x : 1000, y : 110에나타난미사일수를표시합니다.

      buffg.drawString("Enemy Count : " + Enemy_List.size(), 1000, 130);
      // 좌표x : 1000, y : 130에나타난적의수를표시합니다.

   }

/*
* 키보드입력처리부분
*/

   public void KeyProcess() {
      if (KeyUp == true) {
         if (y > 20)
            y -= 5;
         // 캐릭터가 보여지는 화면 위로 못넘어가게합니다.

         player_Status = 0;
         // 이동키가 눌려지면 플레이어상태를 0으로돌립니다.
      }

      if (KeyDown == true) {
         if (y + Player_img[0].getHeight(null) < f_height)
            y += 5;
         // 캐릭터가 보여지는 화면 아래로 못넘어가게합니다.

         player_Status = 0;
         // 이동키가 눌려지면 플레이어상태를 0으로 돌립니다.
      }

      if (KeyLeft == true) {
         if (x > 0)
            x -= 5;
         // 캐릭터가 보여지는 화면왼쪽으로 못넘어가게합니다.

         player_Status = 0;
         // 이동키가 눌려지면 플레이어 상태를 0으로 돌립니다.
      }

      if (KeyRight == true) {
         if (x + Player_img[0].getWidth(null) < f_width)
            x += 5;
         // 캐릭터가 보여지는 화면 오른쪽으로 못 넘어가게합니다.

         player_Status = 0;
         // 이동키가 눌려지면 플레이어상태를 0으로 돌립니다.
      }
   }

   public void keyPressed(KeyEvent e) {

      switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
         KeyUp = true;
         break;
      case KeyEvent.VK_DOWN:
         KeyDown = true;
         break;
      case KeyEvent.VK_LEFT:
         KeyLeft = true;
         break;
      case KeyEvent.VK_RIGHT:
         KeyRight = true;
         break;

      case KeyEvent.VK_SPACE:
         KeySpace = true;
         break;
      }
   }

   public void keyReleased(KeyEvent e) {

      switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
         KeyUp = false;
         break;
      case KeyEvent.VK_DOWN:
         KeyDown = false;
         break;
      case KeyEvent.VK_LEFT:
         KeyLeft = false;
         break;
      case KeyEvent.VK_RIGHT:
         KeyRight = false;
         break;

      case KeyEvent.VK_SPACE:
         KeySpace = false;
         break;

      }
   }

   public void keyTyped(KeyEvent e) {}
   //없으면 에러가 나므로 생성
   }

/*
* 객체화를위한클래스관리부분
*/

   class Missile {
      // 여러개의 미사일 이미지를 그리기 위해 클래스를 추가
      int x ;//미사일 현재x 좌표용 변수
      int y; //미사일 현재y 좌표용 변수
      int angle; // 미사일이 날라가는 방향 판별을 위한 변수
      int speed; //미사일 움직임 속도 변수
      int who;//미사일이 발사한 것이 누군지 구분하는 변수

      Missile(int x, int y, int angle, int speed, int who) {
         this.x = x;
         this.y = y;
         this.who = who;
         //추가된변수를받아옵니다.
         this.angle = angle;
         this.speed = speed;

      }

      public void move() {
         x += Math.cos(Math.toRadians(angle)) * speed;
         // 해당 방향으로 미사일발사.
         y += Math.sin(Math.toRadians(angle)) * speed;
         // 해당 방향으로 미사일발사.
      }
   }

   class Enemy {
      // 여러개의 적 이미지를 그리기 위해 클래스를 추가하여 객체관리
      int x;//적현재x 좌표용변수
      int y;//적현재y 좌표용변수
      int speed; // 적이동속도변수를추가

      Enemy(int x, int y, int speed) {
         this.x = x;
         this.y = y;
         this.speed = speed;
      }

      public void move() {
         x -= speed;// 적이동속도만큼이동
      }
   }

   class Explosion {
      // 여러개의 폭발 이미지를 그리기 위해 클래스를 추가

      int x; // 이미지를그릴x 좌표
      int y; // 이미지를그릴y 좌표
      int ex_cnt; // 이미지를 순차적으로 그리기 위한 카운터
      int damage; // 이미지 종류를 구분하기 위한 변수 값

      Explosion(int x, int y, int damage) {
         this.x = x;
         this.y = y;
         this.damage = damage;
         ex_cnt = 0;
      }

      public void effect() {
         ex_cnt++;
      }
   }