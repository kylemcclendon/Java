//Saves a screenshot (image) in the "screenshots" folder
//Tarah Marlow

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
  
}

void keyReleased(){
  if (key == 's'){
    saveFrame("screenshots/image####.png");
  }
}
