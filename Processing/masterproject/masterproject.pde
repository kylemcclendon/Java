import processing.video.*;
import java.awt.Frame;
import java.util.Collections;
import ddf.minim.*;

// webcam capture
Capture cam;
PFrame f;
secondApplet s;
// brightness
static final float STARTING_BRIGHTNESS = 1;
float bright_up = STARTING_BRIGHTNESS;
// color matching
static final float COLOR_MATCH_THRESHOLD = 65;
static final int STEP_SIZE = 8;
PImage manipulatedFrame;
color targetColor;
ArrayList<Shape> shapes = new ArrayList<Shape>();
ViewState viewState = ViewState.RAW_CAPTURE;
// pikachu
PImage[] pikarunL;
PImage[] pikarunR;
PImage[] pikaidle;
int pikaX, pikaY, pikaframe = 0;
PikaState pikaState = PikaState.IDLE;
// how many pixels pikachu moves each step
int PIKA_STEP_SIZE = 4;
// how far pikachu can be from the center of the object
// and still go into idle anim (should be at least one step)
int PIKA_IDLE_TOLERANCE = 3 * PIKA_STEP_SIZE;
ArrayList<PokeBall> balls = new ArrayList<PokeBall>();
PImage ballImg;
int score = 0;
// how far pikachu has to be from a ball in order to collect it
int PIKA_BALL_COLLECT_TOLERANCE = 8;
// sound
Minim minim;
AudioPlayer pikaVoice;

/**********************WEBCAM WINDOW**********************/
void setup() 
{  
  frame.setTitle("WEBCAM");
  size(640, 480);
  PFrame f = new PFrame();
  String[] cameras = Capture.list();
  for (String s : cameras) {
    println(s);
  }
  cam = new Capture(this, width, height, "Webcam C170", 30);
  //cam = new Capture(this, width, height);
  cam.start();            
  //targetColor = color(0, 255, 63);
  targetColor = color(140, 40, 130);
  
  pikarunL = new PImage[2];
  pikarunL[0] = loadImage("pikarunL1.png");
  pikarunL[1] = loadImage("pikarunL2.png");
  pikarunR = new PImage[2];
  pikarunR[0] = loadImage("pikarunR1.png");
  pikarunR[1] = loadImage("pikarunR2.png");
  
  pikaidle = new PImage[1];
  pikaidle[0] = loadImage("pikaidle.png");
  
  pikaState = PikaState.IDLE;
  pikaX = width/2;
  pikaY = height/2;
  
  ballImg = loadImage("pokeball.png");
  
  minim = new Minim(this);
  pikaVoice = minim.loadFile("pikachu.wav");
}

void draw()
{ 
  if (cam.available())
    cam.read();

  if (bright_up != STARTING_BRIGHTNESS)
  {
    manipulatedFrame = brightness_change(cam);
  }
  else {
    manipulatedFrame = cam;
  }

  if (viewState.equals(ViewState.RAW_CAPTURE)) {
    image(manipulatedFrame, 0, 0);
    return;
  }
  
  PImage binaryFrame = trackObject(manipulatedFrame, targetColor); 
  if (viewState.equals(ViewState.BINARIZED)) {
    image(binaryFrame, 0, 0);
  }
  else {
    image(manipulatedFrame, 0, 0);
  }
  
  int x = 0, y = 0;
  if (shapes.size() > 0) {
    Collections.sort(shapes);
    Shape biggestShape = shapes.get(0);
    biggestShape.shapeColor = color(255, 0, 0);
    x = (biggestShape.minX + biggestShape.maxX)/2;
    y = (biggestShape.minY + biggestShape.maxY)/2;
    if (viewState.equals(ViewState.BINARIZED)) {
      stroke(0, 0, 255);
      rect(x-2, y-2, 3, 3);
    }
  }
  if (viewState.equals(ViewState.BINARIZED))
    return;
  
  boolean moving = false;
  // take a step towards the center of the object
  if (x < pikaX - PIKA_IDLE_TOLERANCE) {
    // move left
    moving = true;
    changePikaState(PikaState.MOVING_LEFT);
    pikaX -= PIKA_STEP_SIZE;
  }
  else if (x > pikaX + PIKA_IDLE_TOLERANCE) {
    moving = true;
    changePikaState(PikaState.MOVING_RIGHT);
    pikaX += PIKA_STEP_SIZE;
  }
  if (y < pikaY - PIKA_IDLE_TOLERANCE) {
    moving = true;
    if (pikaState.equals(PikaState.IDLE))
      changePikaState(PikaState.MOVING_LEFT);
    pikaY -= PIKA_STEP_SIZE;
  }
  else if (y > pikaY + PIKA_IDLE_TOLERANCE) {
    moving = true;
    if (pikaState.equals(PikaState.IDLE))
      changePikaState(PikaState.MOVING_RIGHT);
    pikaY += PIKA_STEP_SIZE;
  }
  if (!moving)
    changePikaState(PikaState.IDLE);
  PImage pikaImage = getPikaImage();
  image(pikaImage, pikaX, pikaY);
  pikaframe = (pikaframe + 1) % getCurrentPikaAnimLength();
  
  for (int i = 0; i < balls.size(); i++) {
    PokeBall b = balls.get(i);
    int pikaDistX = abs(pikaX + pikaImage.width/2 - (b.x + ballImg.width/2));
    int pikaDistY = abs(pikaY + pikaImage.height/2 - (b.y + ballImg.height/2));
    if (pikaDistX < PIKA_BALL_COLLECT_TOLERANCE &&
        pikaDistY < PIKA_BALL_COLLECT_TOLERANCE) {
      balls.remove(b);
      i--;
      score++;
      pikaVoice.play();
      pikaVoice.rewind();
    }
    else {
      image(ballImg, b.x, b.y);
    }
  }
}

void changePikaState(PikaState newState) {
  if (pikaState.equals(newState)) {
    return;
  }
  else {
    pikaState = newState;
    pikaframe = 0;
  }
}

PImage getPikaImage() {
  switch (pikaState) {
    case IDLE: return pikaidle[pikaframe];
    case MOVING_LEFT: return pikarunL[pikaframe];
    case MOVING_RIGHT: return pikarunR[pikaframe];
    default: return null;
  }
}
int getCurrentPikaAnimLength() {
  switch (pikaState) {
    case IDLE: return pikaidle.length;
    case MOVING_LEFT: return pikarunL.length;
    case MOVING_RIGHT: return pikarunR.length;
    default: return 1;
  }
}

void saveScreenshot()
{
  saveFrame("screenshots/image####.png");
  println("Saved a screenshot.");
}

PImage trackObject(PImage img, color c)
{
  PImage newImg = binarize(img);
  grassFire(newImg);
  return newImg;
}

PImage binarize(PImage bin) {
  PImage newBin = createImage(bin.width, bin.height, RGB);
  bin.loadPixels();
  newBin.loadPixels();
  for (int i = 0; i < bin.pixels.length; i++) {
    if (euclideanDistance(bin.pixels[i], targetColor) < COLOR_MATCH_THRESHOLD) {
      newBin.pixels[i] = color(0, 0, 0);
    }
    else
    {
      newBin.pixels[i] = color(255, 255, 255);
    }
  }
  newBin.updatePixels();
  return newBin;
}

float euclideanDistance(color c1, color c2) {
  float r = sq(red(c1) - red(c2));
  float g = sq(green(c1) - green(c2));
  float b = sq(blue(c1) - blue(c2));
  return sqrt(r+g+b);
}

void mouseReleased() {
  color ci = manipulatedFrame.get(mouseX, mouseY);
  //color cf = manipulatedFrame.get(mouseX, mouseY);
  //println(red(ci), green(ci), blue(ci), "--", euclideanDistance(ci, targetColor));
  targetColor = ci;
}

PImage brightness_change(Capture vid)
{
  vid.loadPixels();
  PImage target = createImage(vid.width, vid.height, RGB);
  for (int i = 0; i < vid.pixels.length; i++)
  {
    color c = vid.pixels[i];
    float r = red(c)*bright_up;
    float g = green(c)*bright_up;
    float b = blue(c)*bright_up;
    r = clamp(r, 0, 255);
    g = clamp(g, 0, 255);
    b = clamp(b, 0, 255);
    target.pixels[i] = color(r, g, b);
  }
  return target;
}

float clamp(float val, float min, float max) {
  return max(min, min(max, val));
}

void grassFire(PImage img) {
  shapes = new ArrayList<Shape>();
  for (int y = 0; y < img.height; y += STEP_SIZE)
  {
    for (int x = 0; x < img.width; x += STEP_SIZE)
    {
      if (img.get(x, y) == color(0)) {
        color SC = color(0);
        int shapeNum = shapes.size();

        /*if(shapeNum == 0){
         float r = random(10, 250);
         float g = random(10, 250);
         float b = random(10, 250);
         Shape newShape =  new Shape(x, y, x, y, shapeNum, color(r, g, b));
         shapes.add(newShape);
         }*/

        boolean found = false;

        for (int i = 0; i < shapes.size(); i++) {
          if (x <= shapes.get(i).maxX && x >= shapes.get(i).minX &&
            y <= shapes.get(i).maxY && y >= shapes.get(i).minY) {
            //Part of a shape
            //println("part");
            shapeNum = i;
            SC = shapes.get(i).shapeColor;
            found = true;
            break;
          }
        }

        if (!found) {
          //println("new");
          float r = random(10, 250);
          float g = random(10, 250);
          float b = random(10, 250);
          Shape newShape =  new Shape(x, x, y, y, shapeNum, color(r, g, b));
          SC = color(r, g, b);
          shapes.add(newShape);
        }


        label(img, x, y, SC, shapeNum);
        //println(shapes.get(shapeNum).minX, shapes.get(shapeNum).maxX,
        //  shapes.get(shapeNum).minY, shapes.get(shapeNum).maxY);
        //System.exit(0);
      }
    }
  }
}

void label(PImage img, int x, int y, color p, int shapeNum) {
  if (red(img.get(x, y)) == 0)
  {
    //println(x, y);
    img.set(x, y, p);
    if (x < shapes.get(shapeNum).minX) {
      shapes.get(shapeNum).minX = x;
    }
    if (x > shapes.get(shapeNum).maxX) {
      shapes.get(shapeNum).maxX = x;
    }
    if (y > shapes.get(shapeNum).maxY) {
      shapes.get(shapeNum).maxY = y;
    }

    if (x-7 >= 0) {
      label(img, x-7, y, p, shapeNum);
    }
    if (x+7 < img.width) {
      label(img, x+7, y, p, shapeNum);
    }
    /*if(y-8 >= 0){
     label(img, x, y-8, p);
     }*/
    if (y+7 < img.height) {
      label(img, x, y+7, p, shapeNum);
    }
  }
}

void keyPressed() {
  if (key == ' ') {
    Collections.sort(shapes);
    shapes.get(0).shapeColor = color(255, 0, 0);
    println(shapes);
  }
  else if (key == 'p') {
    pikaX = width/2;
    pikaY = height/2;
  }
  else if (key == 'o') {
    println("Pikachu:", pikaX, pikaY, pikaState.name());
    Collections.sort(shapes);
    Shape biggestShape = shapes.get(0);
    int x = (biggestShape.minX + biggestShape.maxX)/2;
    int y = (biggestShape.minY + biggestShape.maxY)/2;
    println("Center:", x, y);
  }
  else if (key == 'b') {
    balls.add(new PokeBall());
  }
}

/**********************CONTROL PANEL WINDOW**********************/
public class PFrame extends Frame
{
  public PFrame()
  {
    setTitle("CONTROL PANEL");
    setBounds(30, 30, 400, 500);
    s = new secondApplet();
    add(s);
    s.init();
    show();
  }
}

public class secondApplet extends PApplet
{
  String[] members = {
    "View Raw Webcam Capture", "View Binarized Video", 
    "View Manipulated Video", "Save Screenshot", 
    "Darken              Brighten"
  };

  public void setup()
  {
  }

  public void draw() 
  {
    background(60, 172, 222);  
    rectMode(CENTER);
    textAlign(CENTER, CENTER);
    textSize(16);
    int btnY = 78;
    for (int i=0; i<5; i++)
    {
      // highlight selected button
      if (i == viewState.ordinal()) {
        fill(131, 217, 255);
      }
      else {
        fill(255);
      }
      rect(width/2, btnY, 300, 50);
      fill(0);
      text(members[i], width/2, btnY); 
      btnY += 78;
    }
    // show current target color
    text("Target Color:", 313, 6);
    fill(targetColor);
    rect(width-8, 8, 16, 16);
    
    if (score > 0) {
      fill(0);
      text("Score:", 25, 6);
      text(score, 58, 6);
    }
  }

  void mousePressed()
  {
    if (s.mouseX > 40 && s.mouseX < 340 && s.mouseY > 54 && s.mouseY < 102) 
    {
      viewState = ViewState.RAW_CAPTURE;
    }
    else if (s.mouseX > 40 && s.mouseX < 340 && s.mouseY > 132 && s.mouseY < 180) 
    {
      viewState = ViewState.BINARIZED;
    }
    else if (s.mouseX > 40 && s.mouseX < 340 && s.mouseY > 210 && s.mouseY < 258) 
    {
      viewState = ViewState.MANIPULATED;
    }
    else if (s.mouseX > 40 && s.mouseX < 340 && s.mouseY > 287 && s.mouseY < 337) 
    {
      saveScreenshot();
    } 
    else if (s.mouseX > 40 && s.mouseX < 190 && s.mouseY > 366 && s.mouseY < 415) 
    {
      bright_up -= 0.1;
    } 
    else if (s.mouseX > 190 && s.mouseX < 340 && s.mouseY > 366 && s.mouseY < 415) 
    {
      bright_up += 0.1;
    }
  }
}

class Shape implements Comparable<Shape> {
  int minX;
  int maxX;
  int minY;
  int maxY;
  int shapeNum;
  color shapeColor;

  public Shape(int lx, int hx, int ly, int hy, int sN, color c) {
    minX = lx;
    maxX = hx;
    minY = ly;
    maxY = hy;
    shapeNum = sN;
    shapeColor = c;
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("[Shape " + shapeNum + ": X");
    s.append(minX + "-" + maxX + ", Y");
    s.append(minY + "-" + maxY + ", #");
    s.append(hex(shapeColor).substring(2) + "]");
    return s.toString();
  }

  public Integer getSize() {
    return (maxX-minX) * (maxY-minY);
  }

  public int compareTo(Shape other) {
    // This is intentionally reversed to make the largest shape
    // sort to the front of the list.
    return other.getSize().compareTo(this.getSize());
  }
}

class PokeBall {
  int x;
  int y;
  public PokeBall() {
    x = floor(random(0, width+1));
    y = floor(random(0, height+1));
  }
}
