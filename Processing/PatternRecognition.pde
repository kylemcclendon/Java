String fname = "testImage.png";
PImage still, binary;
PImage display;  //Image to display
color chosen;
ArrayList<Shape> shapes = new ArrayList<Shape>();

void setup() {
  chosen = color(0, 255, 63);
  still = loadImage(fname);
  binary = still.get();
  binarize(binary);
  grassFire(binary);
  display = binary;
  size(display.width, display.height);
}

void draw() {
  image(display, 0, 0);
}

void binarize(PImage bin){
  for(int i = 0; i < bin.pixels.length; i++){
    if(bin.pixels[i] == chosen){
      bin.pixels[i] = color(0,0,0);
    }
    else
    {
      bin.pixels[i] = color(255,255,255);
    }
  }
}

void grassFire(PImage img) {
  for(int y = 0; y < img.height; y++)
  {
    for(int x = 0; x < img.width; x++)
    {
      if(img.get(x,y) == color(0)){
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
        
        for(int i = 0; i < shapes.size(); i++){
          if(x <= shapes.get(i).maxX && x >= shapes.get(i).minX &&
             y <= shapes.get(i).maxY && y >= shapes.get(i).minY){
            //Part of a shape
           //println("part");
            shapeNum = i;
            SC = shapes.get(i).shapeColor;
            found = true;
            break;
          }
        }
        
        if(!found){
           println("new");
            float r = random(10, 250);
            float g = random(10, 250);
            float b = random(10, 250);
            Shape newShape =  new Shape(x, x, y, y, shapeNum, color(r, g, b));
            SC = color(r, g, b);
            shapes.add(newShape);
         }
         
     
        label(img, x, y, SC, shapeNum);
        //println(shapes.get(shapeNum).minX, shapes.get(shapeNum).maxX, shapes.get(shapeNum).minY, shapes.get(shapeNum).maxY);
        //System.exit(0); 
      }
    }
  }
}

void label(PImage img, int x, int y, color p, int shapeNum) {
  if(red(img.get(x,y)) == 0)
  {
    //println(x, y);
    img.set(x,y,p);
    if(x < shapes.get(shapeNum).minX){
      shapes.get(shapeNum).minX = x;
    }
    if(x > shapes.get(shapeNum).maxX){
      shapes.get(shapeNum).maxX = x;
    }
    if(y > shapes.get(shapeNum).maxY){
      shapes.get(shapeNum).maxY = y;
    }
    
    if(x-7 >= 0){
      label(img, x-7, y, p, shapeNum);
    }
    if(x+7 < img.width){
      label(img, x+7, y, p, shapeNum);
    }
    /*if(y-8 >= 0){
      label(img, x, y-8, p);
    }*/
    if(y+7 < img.height){
      label(img, x, y+7, p, shapeNum);
    }
  }
}

void mouseReleased(){
  println(mouseX, mouseY);
}

class Shape{
  int minX;
  int maxX;
  int minY;
  int maxY;
  int shapeNum;
  color shapeColor;

 public Shape(int lx, int hx, int ly, int hy, int sN, color c){
   minX = lx;
   maxX = hx;
   minY = ly;
   maxY = hy;
   shapeNum = sN;
   shapeColor = c;
 }
}
