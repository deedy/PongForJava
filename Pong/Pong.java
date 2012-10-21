

import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

/** An instance is the game breakout. Start it by executing
    Breakout.main(null);
    */
public class Pong extends GraphicsProgram implements MouseListener{
    
    /** Width of the game display (all coordinates are in pixels) */
    private static final int GAME_WIDTH= 480;
    /** Height of the game display */
    private static final int GAME_HEIGHT= 620;
    
    /** Width of the paddle */
    private static final int PADDLE_WIDTH= 100; 
    /** Height of the paddle */
    private static final int PADDLE_HEIGHT= 11;
    private static final int PADDLE_RADIUS= PADDLE_WIDTH/2;
    /**Distance of the (bottom of the) paddle up from the bottom */
    private static final int PADDLE_OFFSET= 30;
    /**The Paddle */
    private static GRect PADDLE1;
    private static GRect PADDLE2;
    /** Diameter of the ball in pixels */
    private static final int BALL_DIAMETER= 18;
    
    /** Initial slowness of the ball */
    private static final int INIT_SLOWNESS= 5;
    /** Final slowness of the ball after perpetual gameplay */
    private static final int FINAL_SLOWNESS= 2;
    /** Current slowness of the ball */
    private static double SLOWNESS= INIT_SLOWNESS;
    
    /** The Ball */
    private static GOval BALL;
    
    private static GLabel USER_LABEL, CPU_LABEL;
    /** The velocity of the ball in the x and y direction */
    private double vx, vy;
    private double Pvx, Pvy;
    
    
    public static int USER_SCORE, CPU_SCORE;
    /** Number of times Paddle has been hit since beginning of the game */
    private static int PADDLE_HIT_COUNT=0;
    
    
     /** rowColors[i] is the color of row i of bricks */
    private static final Color[] rowColors= {Color.red, Color.red, Color.orange, Color.orange,
        Color.yellow, Color.yellow, Color.green, Color.green,
        Color.cyan, Color.cyan};
    
    /** random number generator */
    private RandomGenerator rgen= new RandomGenerator();
    
    
    /** Run the program as an application. If args contains 2 elements that are positive
        integers, then use the first for the number of bricks per row and the second for
        the number of rows of bricks.
        A hint on how main works. The main program creates an instance of
        the class, giving the constructor the width and height of the graphics
        panel. The system then calls method run() to start the computation.
      */
    public static void main(String[] args) {
       
        String[] sizeArgs= {"width=" + (GAME_WIDTH), "height=" + (GAME_HEIGHT)};
        new Pong().start(sizeArgs);
    }
    
      public void setScoreButton () {
        USER_LABEL=new GLabel (USER_SCORE+"", 0,0);
        USER_LABEL.setFont(new Font("Arial", Font.PLAIN, 80));
        double width = USER_LABEL.getWidth()  +10;
        double height= USER_LABEL.getHeight() +10;
        USER_LABEL.setColor (Color.black);
        USER_LABEL.setLocation (GAME_WIDTH- (3.0/2)*width, (GAME_HEIGHT/2)+0+height);
        CPU_LABEL=new GLabel (USER_SCORE+"", 0,0);
        CPU_LABEL.setFont(new Font("Arial", Font.PLAIN, 80));
        double width1 = CPU_LABEL.getWidth()  +10;
        double height1= CPU_LABEL.getHeight() +10;
        CPU_LABEL.setColor (Color.black);
        CPU_LABEL.setLocation (GAME_WIDTH- (3.0/2)*width1, (GAME_HEIGHT/2)-50);
        add(USER_LABEL);
        add(CPU_LABEL);
        GLine Divider=new GLine (0,(GAME_HEIGHT/2), GAME_WIDTH, (GAME_HEIGHT/2));
        add(Divider);
    }
    
   
    
    
    /** Adds a Paddle of filled black color of PADDLE_WIDTH*PADDLE_HEIGHT dimensions */
    private void addUserPaddle () {
        PADDLE1=new GRect ((GAME_WIDTH-PADDLE_WIDTH)/2, GAME_HEIGHT-(PADDLE_OFFSET+PADDLE_HEIGHT), PADDLE_WIDTH, PADDLE_HEIGHT);
        PADDLE1.setColor (Color.BLACK);
        PADDLE1.setFillColor (Color.BLACK);
        PADDLE1.setFilled (true);
        add (PADDLE1);
    }
    
    /** Adds a Paddle of filled black color of PADDLE_WIDTH*PADDLE_HEIGHT dimensions */
    private void addCPUPaddle () {
        PADDLE2=new GRect ((GAME_WIDTH-PADDLE_WIDTH)/2, PADDLE_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        PADDLE2.setColor (Color.BLACK);
        PADDLE2.setFillColor (Color.BLACK);
        PADDLE2.setFilled (true);
        add (PADDLE2);
    }
    
    /** Sets up Ball in the middle of the playing area with a random x velocity between 1 and 3 and a y velocity of 3, downwards
      * as well as a message to release the ball on clicking */
    private void setUpBall() {
        BALL= new GOval (GAME_WIDTH/2 - (BALL_DIAMETER/2), GAME_HEIGHT/2 - (BALL_DIAMETER/2), BALL_DIAMETER, BALL_DIAMETER);
        BALL.setFillColor (Color.BLACK);
        BALL.setFilled (true);
        add (BALL);
        vx= rgen.nextDouble(1.0, 3.0); 
        if (!rgen.nextBoolean(0.5)) vx= -vx;
        vy=3.0;
        waitForClick();
    }
    
    /** Move the ball one frame, according to the current velocity magnitudes, reflecting them in case a boundary is hit
      * and returning true if the user loses (ball goes below playing area) */
    private int moveBall() {
        GPoint ballPoint = BALL.getLocation();
        if (ballPoint.getX()+BALL_DIAMETER > GAME_WIDTH) vx = -vx;
        if (ballPoint.getX() <= 0) vx = -vx;
        if (ballPoint.getY() <= 0) return 1;
        if (ballPoint.getY() > GAME_HEIGHT) return 2;//ballVY = -ballVY; // basically lost
        BALL.move(vx, vy);
        return 0;
    }
    
    private Boolean movePaddle() {
        GPoint PaddlePoint = PADDLE2.getLocation();
        GPoint ballPoint = BALL.getLocation();
        double cenBall = ballPoint.getX()+(BALL_DIAMETER/2);
        double cenPaddle = PaddlePoint.getX()+(PADDLE_WIDTH/2);
        Pvx = (GAME_WIDTH/40)*(cenBall-cenPaddle)/(GAME_WIDTH/2);
        if (Math.abs (Pvx) <1) Pvx= Pvx<0?-2:2;
         double pos= Math.max( Math.min (PaddlePoint.getX()+Pvx, GAME_WIDTH - PADDLE2.getWidth()), 0);
        PADDLE2.setLocation(pos, PaddlePoint.getY());
        return false;
    }
    
    
    
    /** Returns the colliding object of the BALL or POWER_UP (as they both have the same dimensions) */
    private GObject getCollidingObject(GObject ballObj) {
        GPoint ballPoint = ballObj.getLocation();
        GObject coll;
        GPoint addingPoints[] = new GPoint[4];
        addingPoints[0]= new GPoint(0,0);
        addingPoints[1]= new GPoint(0,BALL_DIAMETER);
        addingPoints[2]= new GPoint(BALL_DIAMETER,BALL_DIAMETER);
        addingPoints[3]= new GPoint(BALL_DIAMETER,0);
        for (int i= 0; i< 4; i++) {
            coll= getElementAt(ballPoint.getX()+addingPoints[i].getX(), ballPoint.getY()+addingPoints[i].getY());
            if (coll!= null)
                return coll;
        }
        return null;
    }
    
            
    /** Sets up all necessary components when the program is run */
    public void setup() {
        setScoreButton();
        addUserPaddle();
        addCPUPaddle();
        setUpBall();
        addMouseListeners();
    }
    
    public void HyperBounceAlgo()
    {
        double Ballx=BALL.getX()+(BALL_DIAMETER/2);
        double Padx = PADDLE1.getX()+ (PADDLE_WIDTH/2);
        vy=-vy;
        double multiplier=(Ballx - Padx)/(PADDLE_WIDTH/4);
         if (Math.abs(multiplier*vx) < 1)
            multiplier=multiplier<0?(-1/vx):(1/vx);
        vx = multiplier*vx;
    }
    
    public void AirHockeyAlgo()
    {
        double signvx=vx>0?1:-1;
        double signvy=vy>0?1:-1;
        double magvx=Math.abs(vx);
        double magvy=Math.abs(vy);
        vx=magvy*signvx;
        vy=-magvx*signvy;
    }

    /** Called when the Ball hits the Paddle. 
      * The noise is played, the slowness is decreased, and the ball is reflected as desired.
      */
    public void PaddleHit () {
        PADDLE_HIT_COUNT++;
        if (SLOWNESS>FINAL_SLOWNESS)
            SLOWNESS-=0.5;
        GPoint ballPoint = BALL.getLocation();
        GPoint paddlePoint = PADDLE1.getLocation();
        if (ballPoint.getY() > GAME_HEIGHT-(PADDLE_OFFSET+PADDLE_HEIGHT+BALL_DIAMETER)) //Makes sure Ball doesnt stick to the paddle 
            BALL.setLocation (ballPoint.getX(),GAME_HEIGHT-(PADDLE_OFFSET+PADDLE_HEIGHT+BALL_DIAMETER));
        
        AirHockeyAlgo();
        
    }
       
     public void Paddle2Hit () {
        PADDLE_HIT_COUNT++;
        if (SLOWNESS>FINAL_SLOWNESS)
            SLOWNESS-=0.5;
        GPoint ballPoint = BALL.getLocation();
        GPoint paddlePoint = PADDLE2.getLocation();
        if (ballPoint.getY() < PADDLE_OFFSET+PADDLE_HEIGHT) //Makes sure Ball doesnt stick to the paddle 
            BALL.setLocation (ballPoint.getX(),PADDLE_OFFSET+PADDLE_HEIGHT);
        
        AirHockeyAlgo();
    }
    /** Runs the major playing component of the game */
    public void play() {
         while(USER_SCORE<10 && CPU_SCORE <10) {
             int x=moveBall();
             if (x==1) {
                 USER_SCORE++;
                 reset();
             }
             else if (x==2) {
                 CPU_SCORE++;
                 reset();
             }
             if (BALL.getY() <GAME_HEIGHT/2)
                 movePaddle();
             pause (SLOWNESS);
             GObject collider = getCollidingObject(BALL);
             if(collider == PADDLE1) {
                 PaddleHit();
             }
             if (collider == PADDLE2) {
                 Paddle2Hit();
             }
         }
         waitForClick();
         removeAll();
         run();
    }
    
     public void reset() {
       
        pause (3000);
        remove(PADDLE1);
        remove(PADDLE2);
        remove(BALL);
        SLOWNESS=INIT_SLOWNESS;
        addUserPaddle();
        addCPUPaddle();
        USER_LABEL.setLabel (USER_SCORE+"");
        CPU_LABEL.setLabel (CPU_SCORE+"");
        if (USER_SCORE<10 && CPU_SCORE <10)
            setUpBall();
    }
    
    /** Run the Breakout program. */
    public void run() {
        setup();
        play();
    }

    
    /** Move the horizontal middle of the paddle to the x-coordinate of the mouse
        -- but keep the paddle completely on the board.
        Called by the system when the mouse is used. 
      */
    public void mouseMoved(MouseEvent e) {
        GPoint p= new GPoint(e.getPoint());
        // Set x to the left edge of the paddle so that the middle of the paddle
        // is where the mouse is --except that the mouse must stay completely
        // in the pane if the mouse moves past the left or right edge.
        double pos= Math.max( Math.min (p.getX()-(PADDLE1.getWidth()/2), GAME_WIDTH - PADDLE1.getWidth()), 0);
        PADDLE1.setLocation((int) pos, GAME_HEIGHT-PADDLE_OFFSET-PADDLE1.getHeight());
    }
    
 
    
    /** = representation of array b: its elements separated by ", " and delimited by [].
        if b == null, return null. */
    public static String toString(String[] b) {
        if (b == null) return null;
        
        String res= "[";
        // inv res contains "[" + elements of b[0..k-1] separated by ", "
        for (int k= 0; k < b.length; k= k+1) {
            if (k > 0)
                res= res + ", ";
            res= res + b[k];
        }
        return res + "]";
    }
}
