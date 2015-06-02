/**
 * 
 */
package com.fs.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** MapActor extends Actor
 * - a subclass of Panel that also displays TiledMapTileLayer.Cell
 * 
 * @author Allen
 *@deprecated
 */
public class MapActor extends Actor {

	final String LOG = "MapActor log : ";
	
	TiledMap tiledMap; //the tiled map
	TiledMapTileLayer tiledLayer; //the tile layer
	TiledMapTileLayer.Cell cell; //will become an actor
	
 	public MapProperties property;
 	
 	Texture panelTexture; //current tile Panel texture (changes)
 	Texture mapTexture;	//current map texture
 	
 	public boolean selected = false;
	public boolean moveableTo = false;
	public boolean view = false;
 		
	public String terrainName;
	public int terrainType;
	Texture texture; //this map tiles textures
 
    //TiledMap actor listeners
    protected ClickListener clickListener;
    
    //A* scores for pathfinding algorithm
//  	public float totalCost; //cost from start + heuristic
//  	public float costFromStart;//distance from start panel to current panel
//  	public float heuristic;//estimated distance from current panel to goal
    
//    public MapActor(TiledMapTile tile, float actorX, float actorY) {
//
//     	this.setX(actorX);
//    	this.setY(actorY);
//     }
//
    /** creates a clickable actor from the TiledMap cells
     * 
     * @param tiledMap : the whole map
     * @param tiledLayer : the Tiled tmx layer
     * @param cell : the cell object
	 * @param x : x position on screen
	 * @param y : y position on screen
     */
    public MapActor(TiledMap tiledMap, TiledMapTileLayer tiledLayer, TiledMapTileLayer.Cell cell, float x, float y) {

        this.tiledMap = tiledMap;
        this.tiledLayer = tiledLayer;
        this.cell = cell;
        this.property = tiledLayer.getProperties();
        this.terrainName = tiledLayer.getName();

		setBounds(x, y, 32, 32); //set Panel actor position
        
        
        if (cell!=null){
	        if (terrainName.equals("panels")){
	        	panelTexture = cell.getTile().getTextureRegion().getTexture();
	        }
	        else{
	            mapTexture = cell.getTile().getTextureRegion().getTexture();
	        }
        
        }
        
         //adds a clicklistener
        addClickListener();
    }
    
 
  
    @Override
	public void draw(Batch batch, float alpha){
		//render();		

    	super.draw(batch, alpha);
    	
//    	if (!terrainName.equals("panels"))
//    		batch.draw(mapTexture, getX(), getY());

     }

	@Override
	public void act(float delta){
		super.act(delta);
		
		//tileTextureChooser();
	}
	
	 

    public void addClickListener() {
    	clickListener = new ClickListener() {
    		@Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log(LOG, ((MapActor)event.getTarget()).cell + " has been clicked.");
            }
    		
    		@Override
    		public boolean isOver(Actor actor, float x, float y){

    			return true;
    		}
    		
    	};
    	
        addListener(clickListener);

//        /* the input listener for this tile
//         * - used to return info related to map property
//         * 	- ie, if tile is water, obstacle or ground
//         */
//        InputListener inputListener = new InputListener() {
//        	int clickCount = 0;
//        	@Override
//			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
//        		MapActor ma = ((MapActor)event.getTarget());
//        		clickCount++;
//        		if (clickCount < 2){
//         			view = true;
//         			selected = true;
//        		}
//
//         		Gdx.app.log(LOG, "this actors property name :  " + ma.terrainName);
//
//         		return true;
//         	}//touch down
//
//         	@Override
//			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//         		Gdx.app.log(LOG, "touch done at (" + x + ", " + y + ")");
//          		if (clickCount > 2){
//         			view = false;
//         			selected = false;
//          		}
//         	}//touch up
//        };
//
//        this.addListener(inputListener);
    }




	/**
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	/**
	 * @return the cell
	 */
	public TiledMapTileLayer.Cell getCell() {
		return cell;
	}

}
