package com.fs.game.screens;

import appwarp.WarpController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.fs.game.assets.Assets;
import com.fs.game.assets.Constants;
import com.fs.game.data.GameData;
import com.fs.game.enums.GameState;
import com.fs.game.main.MainGame;
import com.fs.game.utils.MultiUtils;


public class MainScreen implements Screen {
	
	final MainGame game;

	OrthographicCamera camera;
    Stage stage; //screen stage for click buttons

    public GameState gameState;

    Vector3 touchPoint;

    //for main button/texture to start menu screens
    TextButton welcomeBtn;
    Texture welcomeTex;
    Rectangle welcomeBounds;

    //for test buttons
    Texture[] testTextures;
    private final float[][] testTexPos = {{50f, 200f}, {500f, 200f}, {275f, 75f}};
    Rectangle[] bounds;

    BitmapFont font;
    private final String[] testMgs = Constants.TEST_MGS;


    public MainScreen(final MainGame game) {
		this.game = game;
        this.gameState = GameState.START_SCREEN;
        this.touchPoint = new Vector3();

        this.font = Assets.uiSkin.getFont("retro2");
        this.font.setColor(Color.BLUE);

        GameData.playerName = MultiUtils.setupUsername();
        setupTextures();
        setupCamera();
        setupStage();
    }

    public void setupTextures(){
        welcomeTex = Assets.uiSkin.get("welcomeTex", Texture.class);
        welcomeBounds = new Rectangle(Constants.SCREENWIDTH/2 - 350/2, Constants.SCREENHEIGHT - 120f, 350, 100);

        testTextures = new Texture[3]; //currently 3 tests
        bounds = new Rectangle[3];
        for (int i = 0; i < 3; i++){
            testTextures[i] = Assets.uiSkin.get("testTex", Texture.class);

            bounds[i] = new Rectangle(testTexPos[i][0], testTexPos[i][1],
                    testTextures[i].getWidth(), testTextures[i].getHeight());
        }

    }


    public void setupCamera(){
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 500);
    }

    public void setupStage(){
        stage = new Stage();

        welcomeBtn = new TextButton(Constants.WELCOME, Assets.uiSkin, "startStyle1");
        welcomeBtn.setBounds(Constants.SCREENWIDTH/2 - 350/2, Constants.SCREENHEIGHT - 120f, 350f, 100f);

        stage.addActor(welcomeBtn);

    }

    public void startMultiplayer(){
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                game.setScreen(new StartMultiplayerScreen(game, MainScreen.this));
            }
        });
    }


    public void updateScreen(){

        if (welcomeBtn.isPressed()) {
            gameState = GameState.MAIN_MENU;
        }

        /*
         * Sets whether the BACK button on Android should be caught.
         * This will prevent the app from being paused.
         * Will have no effect on the desktop.
         */
        Gdx.input.setCatchBackKey(true);


        if (Gdx.input.justTouched()){
            camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if(welcomeBounds.contains(touchPoint.x, touchPoint.y)){
                gameState = GameState.MAIN_MENU;
            }

            if (bounds[0].contains(touchPoint.x, touchPoint.y)){
                GameData.testType = 1;
                gameState = GameState.RUN;
            }

            if (bounds[1].contains(touchPoint.x, touchPoint.y)){
                GameData.testType = 2;
                gameState = GameState.RUN;
            }

            if (bounds[2].contains(touchPoint.x, touchPoint.y)){
                GameData.testType = 3;
                gameState = GameState.MULTIPLAYER;
            }

        }
        else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            gameState = GameState.MAIN_MENU;
        }
        //a regular game setup of what game play will look like normally
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            //the default test setup; how game play will look with full array of units for both players
            GameData.testType = 1;
            gameState = GameState.RUN;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            GameData.testType = 2;
            gameState = GameState.RUN;
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)){
            GameData.testType = 3;
            startMultiplayer();

        }
        else{
            gameState = GameState.START_SCREEN;
        }

        stage.act();
    }


    @Override
    public void render(float delta) {

        switch(gameState){
            case START_SCREEN:
                updateScreen();
                show();
                break;
            case MAIN_MENU:
                game.setScreen(game.getMenuScreen());
                break;
            case RUN:
                game.setScreen(new GameScreen(game));
                break;
            case MULTIPLAYER:

                WarpController.getInstance().startApp(GameData.playerName); //starts appwarp

                System.out.println("Player name: " + GameData.playerName);
                game.setScreen(new StartMultiplayerScreen(game, MainScreen.this));
                //startMultiplayer();
                break;
            case QUIT:
                WarpController.getInstance().handleLeave();
                updateScreen();
                show();
                break;
        }

    }


    @Override
	public void show() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);


        game.batch.begin();

        //draw textures
        game.batch.draw(welcomeTex, Constants.SCREENWIDTH/2 - 350/2, Constants.SCREENHEIGHT - 120f);
        font.draw(game.batch, Constants.WELCOME, Constants.SCREENWIDTH/2 - 350/2 + 40f, Constants.SCREENHEIGHT - 80f);

        for (int i = 0; i < 3; i++){

            game.batch.draw(testTextures[i], testTexPos[i][0], testTexPos[i][1]);

            float x = testTexPos[i][0] + testTextures[i].getWidth()/4;
            float y = testTexPos[i][1] + testTextures[i].getHeight()/2;
            font.drawMultiLine(game.batch, testMgs[i], x, y, (float)testTextures[i].getWidth()-20f, HAlignment.CENTER);
        }

        game.batch.end();

        //draw the stage TODO: keep either stage or Textures w/ Rectangles
        stage.draw();

	}

	@Override
	public void hide() {

    }

	@Override
	public void pause() {

		
	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {

	}


    @Override
    public void resize(int width, int height) {

    }
}
