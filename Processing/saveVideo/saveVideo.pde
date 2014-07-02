/* This program will save a sequence of images in a "movie" folder.
    To convert these to an image, use Processing's Movie Maker tool in "Tools" > "Movie Maker".
    
    Note from Processing: 
    Movie Maker – the MovieMaker class has been removed, because it was specific to QuickTime for Java. 
    In its place there is now a Movie Maker item under the Tools menu, 
    that helps you convert a file of frames into a video file. 
    There isn't a good library-based method to make this work, 
    so it'll probably stay a Tool rather than be re-incorporated into the video library.
*/
//Tarah Marlow

boolean recording;

void setup(){
  recording = false;
}

int x = 0;
void draw(){
  /* begin test code */
  background(204);
  if (x < 100){
    line(x, 0, x, 100);
    x = x + 1;
  } 
  else{
    noLoop();
  }
  /* end test code */
  
  if(recording){
   saveFrame("movie/frames####.png");
  }
}

void keyReleased(){
  if (key == 'r'){ //or 'v' if you guys prefer that better
    if(recording){
      recording = false;
    }
    else{
      recording = true;
    }
  }
}
