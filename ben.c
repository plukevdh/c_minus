void test(void){
  int a;
  int b;
  a = 0;
  b = 1;
  a = b = 0;
  if(a > b){
    b = b - 1;
  }
  else{
    b = 2;
    while( b == 2){
      b = 2;
      if(b == 2){
        b = b + 1;
      }
      else{
        b = 1;
        while(b == 1){
          b = b + 2;
        }
      }
    }
  }
  if(a == b){
    while (a == b){
      while(a == b){
        a = b  - 1;
      }
    }
  }
  b = 3;
  return;
}