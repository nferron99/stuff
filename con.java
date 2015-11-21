
    /*----------------------------------------------------
    //  viz CFD simulation data
    //  coded by Ji Zhang
    //  http://episode-hopezh.blogspot.sg
    //  Apr 2013
    ----------------------------------------------------*/
    import processing.opengl.*;
    import peasy.*;
    PeasyCam cam;
    import org.gicentre.utils.colour.*; // color scheme library
    ArrayList<Cell> cells = new ArrayList(); //current arraylist to be used in draw()
    ArrayList<Cell> cellsTemp = new ArrayList(); //temp arraylist to store newly loaded data
    boolean newDataLoaded = false;
    boolean displayPt  = true;
    boolean showColor = true;
    float vecScale = 1;
    int numCells;
    String csvFilePathAndName_str = "";

    ////////////////////////////////////////////////////////////////
    void setup(){
      size(1000,800,OPENGL);
      smooth(8);
     
      //use the following to avoid pt been shown in fixed size
      hint(ENABLE_STROKE_PERSPECTIVE);
     
      //_____setup perspective parm
      float fov  = PI/3.0;  // field of view
      float nearClip = 1;
      float farClip = 100000;
      float aspect = float(width)/float(height);
      perspective(fov, aspect, nearClip, farClip);
      cam = new PeasyCam(this, 200); // the smaller the var, the close the distance to the object
    }

    ////////////////////////////////////////////////////////////////
    void draw(){
      background(0);
      if (newDataLoaded == true){
        cells = cellsTemp;
        newDataLoaded = false;
      }
      assignPtColor(cells);
     
      for(Cell c:cells){
        if (displayPt == true){
          c.displayPt();
        }
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

    /*----------------------------------------------------
    load CFD results .csv file and
    assign each row of data to a cell object
    ----------------------------------------------------*/
    void loadCFD(File selection){
     
      if (selection == null) {
        println("Window was closed or the user hit cancel."); // Do nothing.
      } else {
        csvFilePathAndName_str = selection.getAbsolutePath(); //get csv file path and name
      
        Table t = loadTable(csvFilePathAndName_str); //create a Table object to store csv file data
      
        numCells = t.getRowCount() - 6; //exclude the top 6 rows
          
        cellsTemp.clear(); //clear and initiate a new array list of cells for the current set of data
      
        float speedMin = 0, speedMax = 0; //initialize overall min and max speed values
      
        //____create cell objects
        for(int i=0; i<numCells; i++){
          Cell c = new Cell();
          cellsTemp.add(c);
        
          c.id = i;
        
          c.location.x = -t.getFloat(i+6, 0); // x of the poping start point
          c.location.y = -t.getFloat(i+6, 2); // get y position from the "Z" colume in csv
          c.location.z = t.getFloat(i+6, 1);  // get z position from the "Y" colume in csv
        
          c.velocity.x = -t.getFloat(i+6, 5); // get x velocity component
          c.velocity.y = -t.getFloat(i+6, 7); // get y velocity component from the "Z" colume in csv
          c.velocity.z = t.getFloat(i+6, 6);  // get z velocity component from the "Y" colume in csv
        
          c.speed = t.getFloat(i+6, 4); // get speed(velcocity magnitude)
        
          // compare current pt speed with min and max speed and replace them if qualified
          if(c.speed <= speedMin){
            speedMin = c.speed;
          } else if (c.speed > speedMax){
            speedMax = c.speed;
          }  
        }
      
        //_____assign overall min and max speed to each pt
        for(Cell c:cellsTemp){
          c.speed_min = speedMin;
          c.speed_max = speedMax;
        }
     
        // !!! pass the temp ArrayList "cellsTemp" to "cells" which is to be iterated in draw()
        //cells = cellsTemp;
      
        newDataLoaded = true;
      }
    }

    /*----------------------------------------------------
    assign color to pt based on their speed
    as mapped to min to max speeds of the current data set
    ----------------------------------------------------*/
    void assignPtColor(ArrayList<Cell> cells){// !!! note the "ArrayList<Cell>" part
     
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
        c.ptColor = cTable.findColour(map(c.speed, c.speed_min, c.speed_max, 0, 1));
      }
     
    }

    void keyPressed(){
      if ( key == 'f' ) {
        selectInput("select a new .csv file", "loadCFD");
      }
    }
