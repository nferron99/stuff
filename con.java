
    // viz CFD simulation data
    // coded by Ji Zhang
    // http://episode-hopezh.blogspot.sg
    // Apr 2013
    //===================================================================

    import processing.opengl.*;
    import peasy.*;
    PeasyCam cam;
    import org.gicentre.utils.colour.*; // color scheme library
    ArrayList<Cell> cells = new ArrayList();
    boolean displayPt  = true;
    boolean displayVec = true;
    boolean displayValue = false;
    boolean showText = true;
    boolean showColor = true;
    float vecScale = 1;
    int numCells;
    String csvFilePathAndName_str = "";

    //===================================================================
    void setup(){
      size(1000,800,OPENGL);
      smooth(8);
     
      //use the following to avoid pt been shown in fixed size
      hint(ENABLE_STROKE_PERSPECTIVE);
     
      // ---------------------------------------------------------
      // setup perspective parm 
      float fov  = PI/3.0;  // field of view
      float nearClip = 1;
      float farClip = 100000;
      float aspect = float(width)/float(height);
      perspective(fov, aspect, nearClip, farClip);
      cam = new PeasyCam(this, 200); // the smaller the var, the close the distance to the object
    }

    //===================================================================
    void draw(){
      background(0,0,30);

      assignPtColor(cells);
     
      for(Cell c:cells){
        if (displayPt == true){
          c.displayPt();
        }
      }
     
      //_____print information on screen
      if(showText == true){
        pushMatrix();
        hint(DISABLE_DEPTH_TEST);
       
        camera();
        noLights();
       
        // 2D code
        noStroke();
        fill(255,80);
        rect(0,0, width,50);
        rect(0,height-110, width,110);
       
        textSize(15);
        fill(0,255,0);
        text("data file : " + csvFilePathAndName_str, 15,15);
        text("num of cells : " + numCells, 15,30);
        text("vec scale : " + nfc(vecScale,1), 15,45);
        text("[f] choose csv file" , 15,height-90);
        text("[p] show/hide point" , 15,height-75);
        text("[c] B&W or color", 15,height-60);

        if (cells.size() != 0) {
          // color scheme __________________________________________________
          // define a Kelvin color scale
          ColourTable cTable = new ColourTable();
          cTable.addContinuousColourRule(0,   0,   0,   255);  // pure Blue (Value,Red,Grn,Blu)
          cTable.addContinuousColourRule(0.4, 206, 255, 255);  // Pale blue
          cTable.addContinuousColourRule(0.6, 238, 255, 163);  // Yellow-green.
          cTable.addContinuousColourRule(0.7, 255, 255, 0);  // Yellow.
          cTable.addContinuousColourRule(0.8, 240, 165, 0);  // Orange.
          cTable.addContinuousColourRule(1,   255, 0,   0);  // pure Red. 
         
          //_____get min and max speed from the current csv file
          float speedMin = 0, speedMax = 0;
          for(Cell c:cells){
            float speed = c.speed;
            if(speed <= speedMin){
              speedMin = speed;
            } else if (speed > speedMax){
              speedMax = speed;
            }
          }
         
          // draw color legend
          int numBand = 20;
          int bandHeight = 20;
          int bandWidth = 10;

          for (int i=0; i<numBand; i++){
            color c = cTable.findColour(map(i, numBand-1,0, 0,1));
            noStroke();
            fill(c);
            rect(width-bandWidth*(numBand-i)-10, height-bandHeight-10, bandWidth,bandHeight);
          }
         
          textSize(15);
          fill(0,255,0);
          text(nfc(speedMax,2) + "m/s", width-bandWidth*numBand-50, height-bandHeight-10);
          text(0, width-10, height-bandHeight-10);
        }
           
        hint(ENABLE_DEPTH_TEST);
        popMatrix();
      }
    }

    class Cell{
      //_____variables
      int id;
      PVector location = new PVector();
      PVector velocity = new PVector();
      float speed;
      float speed_min;
      float speed_max;
      color ptColor;
      float ptSize = 1;
     
      //==============================================
      void displayPt(){
        if(showColor==true){
          stroke(ptColor);
        } else {stroke(255);}
        strokeWeight(ptSize);
        point(location.x, location.y, location.z);
      }
    }

    void loadCFD(File selection){
     
      if (selection == null) {
        println("Window was closed or the user hit cancel."); // Do nothing.
      } else {
        csvFilePathAndName_str = selection.getAbsolutePath(); //get csv file path and name
       
        Table t = loadTable(csvFilePathAndName_str); //create a Table object to store csv file data
       
        numCells = t.getRowCount() - 6; //exclude the top 6 rows
           
        cells = new ArrayList();

        //____create cell objects
        for(int i=0; i<numCells; i++){
          Cell c = new Cell();
          cells.add(c);
         
          c.id = i;
         
          c.location.x = -t.getFloat(i+6, 0); // x of the poping start point
          c.location.y = -t.getFloat(i+6, 2); // get y position from the "Z" colume in csv
          c.location.z = t.getFloat(i+6, 1);  // get z position from the "Y" colume in csv
         
          c.velocity.x = -t.getFloat(i+6, 5); // get x velocity component
          c.velocity.y = -t.getFloat(i+6, 7); // get y velocity component from the "Z" colume in csv
          c.velocity.z = t.getFloat(i+6, 6);  // get z velocity component from the "Y" colume in csv
         
          c.speed = t.getFloat(i+6, 4); // get speed(velcocity magnitude)   
        }
      }
    }

    ///////////////////////////////////////////////////////////
    void assignPtColor(ArrayList<Cell> cells){// note the ArrayList<Cell> part!!!
      float speedMin = 0, speedMax = 0;

      //_____get min and max speed from the current csv file
      for(Cell c:cells){
        float speed = c.speed;
        if(speed <= speedMin){
          speedMin = speed;
        } else if (speed > speedMax){
          speedMax = speed;
        }
      }
     
      //_____define a Kelvin color scale2
      ColourTable cTable = new ColourTable();
      cTable.addContinuousColourRule(0,   0,   0,   255);  // pure Blue (Value,Red,Grn,Blu)
      cTable.addContinuousColourRule(0.4, 206, 255, 255);  // Pale blue
      cTable.addContinuousColourRule(0.6, 238, 255, 163);  // Yellow-green.
      cTable.addContinuousColourRule(0.7, 255, 255, 0);    // Yellow.
      cTable.addContinuousColourRule(0.8, 240, 165, 0);    // Orange.
      cTable.addContinuousColourRule(1,   255, 0,   0);    // pure Red.
     
      //_____assign color to pt by its speed mapped to overall speedMin & speedMax
      for(Cell c:cells){
        c.ptColor = cTable.findColour(map(c.speed, speedMin, speedMax, 0, 1));
      } 
    }

    void keyPressed(){
      if ( key == 'f' ) {
        selectInput("select a new .csv file", "loadCFD");       
      }
      if ( key == 'p' ) {
        displayPt = (displayPt == true) ? (false):(true);
      } 
      if ( key == '=' ) {
        vecScale += 0.2;
      } 
      if ( key == '-' ) {
        vecScale -= 0.2;
      }
      if ( key == 'h' ) {
        showText = (showText == true) ? (false):(true);
      }
      if ( key == 'c' ) {
        showColor = (showColor == true) ? (false):(true);
      }
    }
