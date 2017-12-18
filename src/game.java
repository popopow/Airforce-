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
      //������� ����      
   }catch(Exception ex) {}

   }
}
   

   class game_Frame extends JFrame implements KeyListener, Runnable {
      
      int f_width; 
      int f_height; 
      int x, y; //�÷��̾�ĳ������������ǥ������������

      int[] cx = { 0, 0, 0 }; // ��潺ũ�Ѽӵ�����뺯��
      int bx = 0; // ��ü��潺ũ�ѿ뺯��

      boolean KeyUp = false; //Ű����Ű������������
      boolean KeyDown = false;
      boolean KeyLeft = false;
      boolean KeyRight = false;
      boolean KeySpace = false;

      int cnt; //���ѷ���Ƚ����ī��Ʈ�Һ���

      int player_Speed;// ������ĳ���Ͱ������̴¼ӵ��������Һ���
      int missile_Speed;// �̻����̳��󰡴¼ӵ������Һ���
      int missile_Speed2;
      int fire_Speed; // �̻��Ͽ���ӵ���������
      int enemy_speed; // ���̵��ӵ�����
      int player_Status = 0;// ����ĳ���ͻ���üũ����0 : ����, 1: �̻��Ϲ߻�, 2: �浹
      int game_Score; // �����������
      int player_Hitpoint; // �÷��̾�ĳ������ü��

      Thread th; //���������

      Image[] Player_img;// �÷��̾��̹���
      Image BackGround_img; // ���ȭ��
      Image[] Cloud_img; // �����̴¹���
      Image[] Explo_img; // ��������Ʈ��

      Image Missile_img; //�÷��̾�̻����̹�������
      Image Enemy_img; // ���̹�������
      Image Missile2_img;// ���̻����̹����߰�����

      ArrayList Missile_List = new ArrayList(); //�ټ��ǹ̻����������ϱ����ѹ迭
      ArrayList Enemy_List = new ArrayList();//�ټ������������ϱ����ѹ迭
      ArrayList Explosion_List = new ArrayList();// �ټ�����������Ʈ��ó���ϱ����ѹ迭

      Image buffImage; 
      Graphics buffg; 

      Missile ms; //�̻���Ŭ��������Ű
      Enemy en; // ���ʹ�Ŭ��������Ű

      Explosion ex; // ��������Ʈ��Ŭ��������Ű

/*
* �����ӻ����κ�
*/

      game_Frame() {//ȭ�鿡�����������ӻ����޼ҵ�
         init();
         start();
         

         setTitle("���ð��Ӹ����");//������Ÿ��Ʋ����
         setSize(f_width, f_height);//������ũ�⼳��

         Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

         int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
         int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
         //�������������ȭ�����߾ӿ���ġ

         setLocation(f_xpos, f_ypos);
         setResizable(false); 
         setVisible(true);
         
      }
      

/*
* �⺻�����κ�
*/
      
      public void init() { //�⺻���ΰ��Ӽ����� ����
         x = 100; //�����÷��̾����x��ǥ
         y = 100; //�����÷��̾����y��ǥ
         f_width = 1200; //�����ӳ���
         f_height = 600; //�����ӳ���

         Missile_img = new ImageIcon("Missile.png").getImage();
         //�÷��̾�̻����̹������Ϲ޾Ƶ��̱�
         Missile2_img = new ImageIcon("Missile2.png").getImage();
         //���̻����̹������Ϲ޾Ƶ��̱�
         Enemy_img = new ImageIcon("enemy_1.png").getImage();
         //���̹������Ϲ޾Ƶ��̱�

         Player_img = new Image[5];
         for (int i = 0; i < Player_img.length ; ++i) {
            Player_img[i] = 
            new ImageIcon("f15k_" + i + ".png").getImage();
         }
         // �÷��̾� �ִϸ��̼�ǥ���� ���� ���� �̸��� ������ �迭�� ��´�

         BackGround_img = new ImageIcon("background.png").getImage();
         // ��ü���ȭ���̹������޽��ϴ�.

         Cloud_img = new Image[3];
         for (int i = 0; i < Cloud_img.length ; ++i) {
            Cloud_img[i] = new ImageIcon("cloud_" + i + ".png").getImage();
         }
         // ������ 3�� ���ÿ� �׸��µ� ���ǻ� �迭�� 3���� ���ÿ��޴´�.

         Explo_img = new Image[3];
         for (int i = 0; i < Explo_img.length ; ++i) {
            Explo_img[i] = 
            new ImageIcon("explo_" + i + ".png").getImage();
         }
         // ���߾ִϸ��̼�ǥ��������
         // �����̸����ѹ����ٳ����迭�δ�´�.

         game_Score = 0;// ���ӽ��ھ��ʱ�ȭ
         player_Hitpoint = 3;// �����÷��̾�ü��

         player_Speed = 5; // ����ĳ���Ϳ����̴¼ӵ�����
         missile_Speed = 11; // �̻��Ͽ����Ӽӵ�����
         missile_Speed2 = 22;
         fire_Speed = 15; // �̻��Ͽ���ӵ�����
         enemy_speed = 3;// ���̳�����¼ӵ�����
      }

      public void start() { //�⺻�����ν����ϴ¸޼ҵ�
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         addKeyListener(this);

         th = new Thread(this);
         //���ο���带����
         th.start();
         //���������      
      }
      
/*
* ������
*/
      public void run() {//���������κ�
         try {
            while (true) { // ���ѷ���������

               KeyProcess();
               EnemyProcess();
               MissileProcess();
               ExplosionProcess();
               //����ó���޼ҵ����

               repaint();

               Thread.sleep(20);

               cnt++;
               
            }
         } catch (Exception e) {}
      }
      

/*
* ����ó���޼ҵ�
*/
      public void MissileProcess() {//�̻��ϰ���ó���޼ҵ�
         if (KeySpace) { //Ű���彺���̽�Ű�Է¿���Ȯ��
            player_Status = 1; //�÷��̾���¸�1�κ���

            if ((cnt % fire_Speed) == 0) { 
               //����ī�����ǰ�����fire_Speed���������Ѱ���ŭ
               //�������ǳ�������0�������θ�Ȯ��

               ms = new Missile(x + 150, y + 30, 0, missile_Speed, 0);
               // �⺻���� ������ ���� �̻���
               // �������� 0�� �⺻ ���Դϴ�.
               //���ʺ��� �̻��� x��ǥ, y��ǥ, �̻��� �������, �̻��� �ӵ�, �̻��� ����
               //�̻��� ���� 0 : �÷��̾ �߻��ϴ� �̻���, 1 : ���� �߻��ϴ� �̻���
               Missile_List.add(ms);

               ms = new Missile(x + 150, y + 30, 330, missile_Speed, 0);
               // ���� �밢������ ���� �̻���
               Missile_List.add(ms);

               ms = new Missile(x + 150, y + 30, 30, missile_Speed, 0);
               // �Ʒ��� �밢������ ���� �̻���
               Missile_List.add(ms);

            }
         }

         for (int i = 0; i < Missile_List.size(); ++i) {
            
            ms = (Missile) Missile_List.get(i);
            //�迭�������ϴ� �̻����ǰ�ü�� �޾ƿ´�.
            ms.move();
            //�ش�̻����������̰Ը����
            if (ms.x > f_width - 20 || ms.x < 0 || ms.y < 0 || ms.y > f_height ) {
               // �ش�̻�����ȭ������γ����°����θ�Ȯ��
               Missile_List.remove(i);
               //ȭ�鳡���������ѹ̻��ϻ���
            }

            if ( Crash(x, y, ms.x, ms.y, Player_img[0], Missile_img) && ms.who == 1 ) {
               //���� �߻��� �̻����� �÷��̾�� �浹
               player_Hitpoint --;
              
               //�÷��̾� ü�� ����Ʈ�� 1�谨
               ex = new Explosion(x, y, 1);
               //�÷��̾� �ڸ��� �浹�� ���� ����Ʈ ��ü����
               Explosion_List.add(ex);
               //������ ��ü�� �迭������
               Missile_List.remove(i);
               //�ش�Ǵ� ���̻��� ����
            }

            for (int j = 0; j < Enemy_List.size(); ++j) {
               en = (Enemy) Enemy_List.get(j);
               //�迭�� �����ϴ� ���� ��ü�� �޾ƿ´�
               if (Crash(ms.x, ms.y, en.x, en.y, Missile_img, Enemy_img) && ms.who == 0) {
                  //�÷��̾�̻��ϰ����浹����
                  
                  Missile_List.remove(i);
                  //�浹�ѹ̻��� ����
                  Enemy_List.remove(j);
                  //�浹�� �� ����

                  game_Score += 10; // ����������+10��.

                  ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2,
                        en.y + Enemy_img.getHeight(null) / 2, 0);
                  // ���� ��ġ�� �ִ� ���� �߽� ��ǥx,y ����
                  // ���߼�����- 0 : ����, 1 : �ܼ��ǰ�

                  Explosion_List.add(ex);
                  // �浹�������� ����� ���� ��ġ��
                  // ����Ʈ�� �߰��Ѵ�.

      
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
      //�̻����� ���� �浹�� ��Ÿ���� �Ҹ�
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
      public void EnemyProcess() {//�����ൿ����ó���޼ҵ�

         for (int i = 0; i < Enemy_List.size(); ++i) {
            en = (Enemy) (Enemy_List.get(i));
            //�ش� �迭�� �� ��ü�� �޾ƿ´�.
            en.move();
            //�ش� �� �����̱�
            if (en.x < -200) { // ����x ��ǥ ���� ȭ�� ������ ���޿���Ȯ��
               Enemy_List.remove(i);
               //ȭ�� ������ ������ �� ����
            }

            if ( cnt % 50 == 0){
               ms = new Missile (en.x, en.y + 25, 180, missile_Speed2, 1);
               //Ȯ�ε� �ش� ���� ��ġ�� �̻��ϻ���
               //���ʺ��� �̻��� x��ǥ, y��ǥ, �̻��� �������, �̻��ϼӵ�, �̻�������
               //�̻������� 0 : �÷��̾�߻��ϴ¹̻���, 1 : ���̹߻��ϴ¹̻���
               Missile_List.add(ms);
               //������ �̻����� ��ü�� �迭�� �߰�
            }


            if (Crash(x, y, en.x, en.y, Player_img[0], Enemy_img)) {
               // �÷��̾�� ���� �浹�� ����

               player_Hitpoint--; // �÷��̾� ü���� 1 ����ϴ�.
               Enemy_List.remove(i); // ���� �����մϴ�.
               game_Score += 10;
               // ���ŵ� ������ ���ӽ��ھ 10 ������ŵ�ϴ�.

               ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2, en.y
                     + Enemy_img.getHeight(null) / 2, 0);
               // ���� ��ġ�� �ִ� ���� �߽���ǥ x,y ��
               // ���߼�����- 0 : ����, 1 : �ܼ��ǰ�

               Explosion_List.add(ex);
               // ���ŵ� �� ��ġ�� ���� ����Ʈ�� �߰��մϴ�.

               ex = new Explosion(x, y, 1);
               // ���� ��ġ�� �ִ� ���� �߽���ǥ x,y ����
               // ���� ������ ���� ��( 0 �Ǵ�1 )�� �޽��ϴ�.
               // ���߼��� ��- 0 : ����, 1 : �ܼ��ǰ�

               Explosion_List.add(ex);
               // �浹�� �÷��̾��� ��ġ�� �浹�� ����Ʈ�� �߰�.

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
            // �� ������ �ӵ��� �߰��� �޾� ���� �����Ѵ�.

         }
      }

      public void ExplosionProcess() {
         // ���� ����Ʈ ó���� �޼ҵ�

         for (int i = 0; i < Explosion_List.size(); ++i) {
            ex = (Explosion) Explosion_List.get(i);
            ex.effect();
            // ����Ʈ�ִϸ��̼�����Ÿ��������
            // ����Ʈó���߰����߻��ϸ��ش�޼ҵ带ȣ��.

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
* �̹����׸��ºκ�
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
   // ����̹������׸��ºκ��Դϴ�.
   
      buffg.clearRect(0, 0, f_width, f_height);
   
      if (bx > -3500) {
   
         buffg.drawImage(BackGround_img, bx, 0, this);
         bx -= 1;
         //����̹�����x��ǥ��
         // ������������̵��Ѵ�. 
   
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
   // 3���� �����̹����� ���� �ٸ� �ӵ������� �������� ������.
   }
   }
   
   public void Draw_Player() {
   //�÷��̾�ĳ���͸��׸��ºκ�
   
   switch (player_Status) {
   
   case 0: // ����
   if ((cnt / 5 % 2) == 0) {
   buffg.drawImage(Player_img[1], x, y, this);
   
   } else {
   buffg.drawImage(Player_img[2], x, y, this);
   }
   
   
   break;
   
   case 1: // �̻��Ϲ߻�
   if ((cnt / 5 % 2) == 0) {
   buffg.drawImage(Player_img[3], x, y, this);
   } else {
   buffg.drawImage(Player_img[4], x, y, this);
   }
   
   player_Status = 0;
   
   break;
   case 2: // �浹
   break;
   
   }
   
   }

   public void Draw_Missile() {
      //�̻����̹������׸��ºκ�
      for (int i = 0; i < Missile_List.size(); ++i) {
         //�̻��Ϲ迭�����������ϸ�
         ms = (Missile) (Missile_List.get(i));
         if ( ms.who == 0 )buffg.drawImage(Missile_img, ms.x, ms.y, this);
         //�÷��̾ �߻��� �̹����� �׸���.
         if ( ms.who == 1 )buffg.drawImage(Missile2_img, ms.x, ms.y, this);
         //���� �߻��� �̹����� �׸���.
      }
   }

   public void Draw_Enemy() {
      //���̹����� �׸��� �κ�
      for (int i = 0; i < Enemy_List.size(); ++i) {
         en = (Enemy) (Enemy_List.get(i));
         buffg.drawImage(Enemy_img, en.x, en.y, this);
      }
   }

   public void Draw_Explosion() {
      // ��������Ʈ�� �׸��� �κ��Դϴ�.

      for (int i = 0; i < Explosion_List.size(); ++i) {
         ex = (Explosion) Explosion_List.get(i);
         // ��������Ʈ�� ���������� üũ

         if (ex.damage == 0) {
            // ��������0 �̸� ���߿� �̹��� �׸���

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
      // ��Ʈ�������մϴ�. �⺻��Ʈ, ����, ������20

      buffg.drawString("SCORE : " + game_Score, 1000, 70);
      // ��ǥx : 1000, y : 70�����ھǥ���մϴ�.

      buffg.drawString("HitPoint : " + player_Hitpoint, 1000, 90);
      // ��ǥx : 1000, y : 90���÷��̾�ü����ǥ���մϴ�.

      buffg.drawString("Missile Count : " + Missile_List.size(), 1000, 110);
      // ��ǥx : 1000, y : 110����Ÿ���̻��ϼ���ǥ���մϴ�.

      buffg.drawString("Enemy Count : " + Enemy_List.size(), 1000, 130);
      // ��ǥx : 1000, y : 130����Ÿ�����Ǽ���ǥ���մϴ�.

   }

/*
* Ű�����Է�ó���κ�
*/

   public void KeyProcess() {
      if (KeyUp == true) {
         if (y > 20)
            y -= 5;
         // ĳ���Ͱ� �������� ȭ�� ���� ���Ѿ���մϴ�.

         player_Status = 0;
         // �̵�Ű�� �������� �÷��̾���¸� 0���ε����ϴ�.
      }

      if (KeyDown == true) {
         if (y + Player_img[0].getHeight(null) < f_height)
            y += 5;
         // ĳ���Ͱ� �������� ȭ�� �Ʒ��� ���Ѿ���մϴ�.

         player_Status = 0;
         // �̵�Ű�� �������� �÷��̾���¸� 0���� �����ϴ�.
      }

      if (KeyLeft == true) {
         if (x > 0)
            x -= 5;
         // ĳ���Ͱ� �������� ȭ��������� ���Ѿ���մϴ�.

         player_Status = 0;
         // �̵�Ű�� �������� �÷��̾� ���¸� 0���� �����ϴ�.
      }

      if (KeyRight == true) {
         if (x + Player_img[0].getWidth(null) < f_width)
            x += 5;
         // ĳ���Ͱ� �������� ȭ�� ���������� �� �Ѿ���մϴ�.

         player_Status = 0;
         // �̵�Ű�� �������� �÷��̾���¸� 0���� �����ϴ�.
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
   //������ ������ ���Ƿ� ����
   }

/*
* ��üȭ������Ŭ���������κ�
*/

   class Missile {
      // �������� �̻��� �̹����� �׸��� ���� Ŭ������ �߰�
      int x ;//�̻��� ����x ��ǥ�� ����
      int y; //�̻��� ����y ��ǥ�� ����
      int angle; // �̻����� ���󰡴� ���� �Ǻ��� ���� ����
      int speed; //�̻��� ������ �ӵ� ����
      int who;//�̻����� �߻��� ���� ������ �����ϴ� ����

      Missile(int x, int y, int angle, int speed, int who) {
         this.x = x;
         this.y = y;
         this.who = who;
         //�߰��Ⱥ������޾ƿɴϴ�.
         this.angle = angle;
         this.speed = speed;

      }

      public void move() {
         x += Math.cos(Math.toRadians(angle)) * speed;
         // �ش� �������� �̻��Ϲ߻�.
         y += Math.sin(Math.toRadians(angle)) * speed;
         // �ش� �������� �̻��Ϲ߻�.
      }
   }

   class Enemy {
      // �������� �� �̹����� �׸��� ���� Ŭ������ �߰��Ͽ� ��ü����
      int x;//������x ��ǥ�뺯��
      int y;//������y ��ǥ�뺯��
      int speed; // ���̵��ӵ��������߰�

      Enemy(int x, int y, int speed) {
         this.x = x;
         this.y = y;
         this.speed = speed;
      }

      public void move() {
         x -= speed;// ���̵��ӵ���ŭ�̵�
      }
   }

   class Explosion {
      // �������� ���� �̹����� �׸��� ���� Ŭ������ �߰�

      int x; // �̹������׸�x ��ǥ
      int y; // �̹������׸�y ��ǥ
      int ex_cnt; // �̹����� ���������� �׸��� ���� ī����
      int damage; // �̹��� ������ �����ϱ� ���� ���� ��

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