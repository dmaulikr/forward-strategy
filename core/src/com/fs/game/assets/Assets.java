/** GameManager
 * - holds gaims main assetmanager
 * - creates a UnitInfo array
 *   - also has method to create damage lists
 *   	damage lists : how much each unit damages all other
 *
 *  TODO: figure out if damageLists should be a seperate JSON
 *
 *
 *
 *  FILES NEEDED:
 *   sky_serpant still pic
 *
 *
 * @author Allen Jagoda
 *
 */
package com.fs.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.fs.game.units.UnitInfo;
import com.fs.game.constants.Constants;
import com.fs.game.utils.UnitUtils;


public class Assets {
	
	//values held in GameManager can be accessed without new instance
	public static AssetManager assetManager; //asset manager for easy access
	public static Skin uiSkin; 	//skin for menu/HUD elements


    public static OrderedMap<String, Array<UnitInfo>> unitInfoMap; //keys are factions, arrays store unitInfo
    public static Array<UnitInfo> unitInfoArray; //1-5 small, 6-8 medium, 9-10 large
    public static Array<int[]> damageListArray;

	/** this returns the asset manager
 	 * TODO: organize, optimize, organize...
	 * @return
	 */
	public static void loadAssets(){
        //holds unitinfo which will be put into a HashMap
        unitInfoMap = new OrderedMap<String, Array<UnitInfo>>();
        unitInfoArray = new Array<UnitInfo>();
        damageListArray = new Array<int[]>();

        Array<UnitInfo> humanInfoArr = new Array<UnitInfo>(); //for storing unit info in map
        Array<UnitInfo> reptoidInfoArr = new Array<UnitInfo>();
        Array<UnitInfo> arthroidInfoArr = new Array<UnitInfo>();

        Array<UnitInfo> unitInfoArray = new Array<UnitInfo>();

		assetManager = new AssetManager();
		
        Json json = new Json();
        JsonValue root = unitInfoFromJSON(json);


        //gets the unit damage lists from UnitDamage json file
        FileHandle handleDL = Gdx.files.internal(Constants.UNIT_DAMAGE_JSON_PATH);
        String jsonAsStringDL = handleDL.readString();
        JsonValue rootDL = new JsonReader().parse(jsonAsStringDL);

		/*
		//GameMananager will hold templates of these scene2d & ui objects
		//GameInfo holds versions of templates updated for current game
		// ie, UnitInfo always holds 7-8 fields, but will vary based on unit
		*/
		for (JsonValue entry = root.child; entry!=null; entry = entry.next) {
            JsonValue dlentry = rootDL.child;
            for (int i = 0; i < entry.size; i++) {
                UnitInfo uInfo = json.fromJson(UnitInfo.class, entry.get(i).toString()); //stores unitInfo

                uInfo.setTexPaths(unitTexturePaths(uInfo));

                //converst size dimensions from String to float value
                float size[] = UnitUtils.Setup.convertStringSizeToFloat(uInfo.getSize());
                uInfo.setWidth(size[0]);
                uInfo.setHeight(size[1]);

                if (dlentry!=null){
                    int[] dl = dlentry.asIntArray();
                    uInfo.setDamageList(dl); //add int[] damageList
                    damageListArray.add(dl);
                    dlentry = dlentry.next;
                }

                if (uInfo.getFaction().equals("Human")) {
                    humanInfoArr.add(uInfo); //store in temporary Array, if 10 units stored, adds to HashMap & clears
                }
                else if (uInfo.getFaction().equals("Reptoid")){
                    reptoidInfoArr.add(uInfo);
                }
                else if (uInfo.getFaction().equals("Arthroid")){
                    arthroidInfoArr.add(uInfo);
                }

                unitInfoArray.add(uInfo); //array storing ALL units
            }//gets all units' info
		}//maps jsonvalue as array to string, then to object UnitInfo, which is stored in array % hashmap

        unitInfoMap.put("Human", humanInfoArr);
        unitInfoMap.put("Reptoid", reptoidInfoArr);
        unitInfoMap.put("Arthroid", arthroidInfoArr);

        //indicates unit can be attacked since enemy is adjacent
        assetManager.load(Constants.ATTACK_FRAME_SMALL, Texture.class);
        assetManager.load(Constants.ATTACK_FRAME_MED, Texture.class);
        assetManager.load(Constants.ATTACK_FRAME_LARGE, Texture.class);


 		//loading the textures for grids on game board
        assetManager.load(Constants.GRID_DOWN_PATH, Texture.class);
        assetManager.load(Constants.GRID_PATH, Texture.class);
        assetManager.load(Constants.PANELS_32X32, Texture.class);

        loadAudio(); //loads the audio into asset manager


		//creates info about units
		//loadUnitDamageLists(arrayUnitInfo); //creates & adds damage lists to current array

		//the assets for anything not related to Unit actor class
		uiSkin = createSkin();

        //set dark themed skin - mainly for testing
        setDarkSkin();
 	}



    /** loads all the audio (sound effects, unit sounds, music, etc)
     * - into the asset manager
     */
    public static void loadAudio(){
        assetManager.load(Constants.music1, Music.class);
    }



    public static JsonValue unitInfoFromJSON(Json json){
        //gets the unitInfo values from JSON file as a String
        FileHandle handle = Gdx.files.internal(Constants.UNIT_STAT_JSON_PATH);
        String jsonAsString = handle.readString();

        //reads JSON file into UnitInfo objects
        UnitInfo unitInfo = new UnitInfo();//create empty object
        json.addClassTag("unitInfo", UnitInfo.class);
        json.toJson(unitInfo, UnitInfo.class);
        json.setIgnoreUnknownFields(true);
        json.setTypeName("UnitInfo");

        JsonValue root = new JsonReader().parse(jsonAsString);

        return root;
    }

    /*
     *  this stores the unit pathways
     * NOTE: some units don't have certain animations
     *   - they are not needed since they cannot move a certain way
     *   - for example, an AirBase can move any way, so has only 1 move animation
     *     while a smaller unit has 4 instead of the 1
     */
    public static Array<String> unitTexturePaths(UnitInfo unitInfo){
        Array<String> unitTexPaths = new Array<String>();
        for (String path : Constants.UNIT_TEX_PATHS){
            String fullPath = unitInfo.getUnitPath()+path;
            if (Gdx.files.internal(fullPath).exists()){
                unitTexPaths.add(fullPath);
                assetManager.load(unitInfo.getUnitPath()+path, Texture.class);
            }
        }

        return unitTexPaths;
    }


	/** creates the skins for unit UI
	 * 
	 * @return skin
	 */
	public static Skin createSkin() {
		Skin skin = new Skin();

		loadFonts(skin); //for fonts
        loadStartScreenAssets(skin);
        loadRuleWindowAssets(skin);
        loadGameScreenAssets(skin); //for gamescreen ui (game play)
        loadMapAssets(skin);

        //------assets for menus-------
        loadMenuAssets(skin);


        loadOtherUI(skin);

		return skin;
	}


    public static void loadFonts(Skin skin){
        /* font acknoledgments from www.pentacom.jp/pentacom/bitfontmaker2/gallery/
		 *  - battlenet by Tom Israels
		 *  - MEGAMAN10 by YahooXD
		 */
        BitmapFont defaultFont = new BitmapFont();
        skin.add("default", defaultFont); //default regular font via libgdx

        defaultFont.scale(.5f); //smaller default font
        skin.add("default-small", defaultFont);

        //For HTML, gdx freetype will not work, so need fnt format with tga images
        //NOTE: when creating fnt from ttf need to set following options in order to process fonts here
        // In font settings: just open the ttf or other font file, make sure font is same as file name, 12-16 bit
        // In Export options: check 32 bit, pack text and outline in same channel, tga texture type
        BitmapFont retro1 = new BitmapFont(Gdx.files.internal(Constants.FONT_BATTLENET_FNT1));
        //BitmapFont retro1 = AssetHelper.fontFNTGenerator(Constants.FONT_BATTLENET, 12, Color.GREEN);
        retro1.setColor(Color.GREEN);
        skin.add("retro1", retro1);

        //BitmapFont retro2 = AssetHelper.fontGenerator(Constants.FONT_MEGAMAN, 16, Color.RED);
        BitmapFont retro2 = new BitmapFont(Gdx.files.internal(Constants.FONT_MEGAMAN_FNT1));
        //BitmapFont retro2 = AssetHelper.fontFNTGenerator(Constants.FONT_MEGAMAN, 12, Color.RED);
        retro2.setColor(Color.RED);
        skin.add("retro2", retro2);

        skin.add("font1", new BitmapFont(Gdx.files.internal(Constants.FONT_DEFAULT1))); //adding custom font (made with BMFont)

        //for damage label of units use retro2
        skin.add("damageFont", retro2);

    }

    public static void loadStartScreenAssets(Skin skin){

        Pixmap pixmap = AssetHelper.createPixmap(60, 30, Color.ORANGE); //back button
        skin.add("backTex", new Texture(pixmap));
        pixmap.setColor(Color.BLACK);
        skin.add("backTexDown", new Texture(pixmap));

        pixmap = AssetHelper.createPixmap(350, 100, Color.CYAN);
        skin.add("welcomeTex", new Texture(pixmap));

        pixmap = AssetHelper.createPixmap(100, 50, Color.CYAN);
        skin.add("rulesTex", new Texture(pixmap));

        pixmap = AssetHelper.createPixmap(325, 60, Color.CYAN);
        skin.add("testTex", new Texture(pixmap));

        TextButtonStyle backStyle = new TextButtonStyle();
        backStyle.up = skin.getDrawable("backTex");
        backStyle.down = skin.getDrawable("backTexDown");
        backStyle.font = skin.getFont("default-small");
        skin.add("backBtnStyle", backStyle);


        TextButtonStyle btnStyle1 = new TextButtonStyle();
        btnStyle1.up = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        btnStyle1.font = skin.getFont("retro2");
//        btnStyle1.font.scale(.7f);
        btnStyle1.fontColor = Color.RED;
        skin.add("startStyle1", btnStyle1);

        TextButtonStyle btnStyle2 = new TextButtonStyle();
        btnStyle2.down = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        btnStyle2.font = skin.getFont("retro1");
        btnStyle2.fontColor = Color.BLUE;
        skin.add("startStyle2", btnStyle2);
    }

    //NOTE: writing to directory works, can write to textures first, then add to skin based on name
    public static void loadRuleWindowAssets(Skin skin){
        Pixmap pixmap = AssetHelper.createPixmap(600, 400, Color.GRAY);
//        AssetHelper.pixmapToFile(pixmap, Constants.ABSOLUTE_ASSETS_DIR +"/skins/default", "rulesBackground.png");
        skin.add("rulesBackground", new Texture(pixmap));

        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.background = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        style.vScroll = new TextureRegionDrawable(new TextureRegion(new Texture(AssetHelper.createPixmap(7, 400, Color.BLACK))));
        style.hScroll = new TextureRegionDrawable(new TextureRegion(new Texture(AssetHelper.createPixmap(600, 7, Color.BLACK))));

        Pixmap vKnobPixmap = AssetHelper.createPixmap(9, 9, Color.DARK_GRAY);
//        AssetHelper.pixmapToFile(vKnobPixmap, Constants.ABSOLUTE_ASSETS_DIR +"/skins/default", "vScrollKnob.png");

        style.vScrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(vKnobPixmap)));
        style.hScrollKnob = style.vScrollKnob; //same as vScrollKnob
        skin.add("ruleScrollStyle", style);

        //pixmap and texture for rules
        Pixmap ruleLabelBack = AssetHelper.createPixmap(7, 400, Color.GRAY);
        AssetHelper.pixmapToFile(ruleLabelBack, Constants.ABSOLUTE_ASSETS_DIR + "/textures/default", "ruLabelBack.png");

        LabelStyle labelRuleStyle = new LabelStyle(); //label style for Rules
        labelRuleStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(ruleLabelBack)));
        labelRuleStyle.font = skin.getFont("default-small");
        skin.add("ruleLabelStyle", labelRuleStyle);

        Window.WindowStyle winStyle = new Window.WindowStyle();
        winStyle.titleFont = skin.getFont("default-small");
        winStyle.titleFont.scale(.01f); //scale it down a bit
        winStyle.stageBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        skin.add("ruleWinStyle", winStyle);

    }

    public static void loadMapAssets(Skin skin){
        skin.add("panelDown", new Texture(Gdx.files.internal("maps/tiles/gridDown.png")));
        skin.add("panelUp", new Texture(Gdx.files.internal("maps/tiles/grid.png")));
        skin.add("panelView", new Texture(Gdx.files.internal("maps/tiles/gridView.png")));
    }




    public static void loadGameScreenAssets(Skin skin){
        //TODO: place these into styles for widgets (will allow for easier widget creation)
        // All UI textures for game screens (GameScreen & MultiplayerScreen)
        skin.add("infoPanDown", new Texture(Gdx.files.internal("infopanel/infoPanelUp.png")));
        skin.add("enemyUInfo", new Texture(Gdx.files.internal("infopanel/enemyUInfo.png")));
        skin.add("mainPanel", new Texture(Gdx.files.internal("infopanel/mainpanel.png")));
        skin.add("infoPane", new Texture(Gdx.files.internal("infopanel/infopanel.png")));
        skin.add("timer", new Texture(Gdx.files.internal("infopanel/timer.png")));

        //w/ additional textures added to skin which are programmatically created
        skin.add("detail-popup", new Texture(AssetHelper.createPixmap(384/2, 100, Color.GRAY)));
        skin.add("damage-popup", new Texture(AssetHelper.createPixmap(384/2, 100, Color.LIGHT_GRAY)));
        skin.add("vert-scroll", new Texture(AssetHelper.createPixmap(10, 100, Color.BLACK)));

        //timer skin - background
        skin.add("timer", new Texture(Gdx.files.internal("infopanel/timer.png")));

        //textures for side panel buttons
        // textures for the advance button
        skin.add("go-tex", new Texture(Gdx.files.internal("sidepanel/go.png")));
        skin.add("stop-tex", new Texture(Gdx.files.internal("sidepanel/stop.png")));


        //w/ some of the widget styles added to skin
        LabelStyle styleDamage = new LabelStyle();
        styleDamage.background = skin.getDrawable("damage-popup");
        styleDamage.font = skin.getFont("retro1");
        skin.add("labelStyle-damage", styleDamage);

        //w/ images added to table which is added to scroll pane
        LabelStyle styleDetail = new LabelStyle();
        styleDetail.background = skin.getDrawable("detail-popup");
        styleDetail.font = skin.getFont("default-small");
        skin.add("labelStyle-detail", styleDetail);

        //some more skins
        // NOTE: at the moment some elements are being generated by the TextureUtils class
        // creates an advance (player turn end) button
        skin.add("lets-go-tex", new Texture(Gdx.files.internal("infopanel/advanceButton.png")));


        skin.add("dmgTex", new Texture(Gdx.files.internal(Constants.DMG_LABL_TEX)));


        Texture textBackTex = new Texture(AssetHelper.createPixmap(100, 100, Color.LIGHT_GRAY));
        skin.add("textBackTex", textBackTex);
    }




    public static void loadMenuAssets(Skin skin){

        mainMenuAssets(skin);
        factionMenuAssets(skin);
        unitMenuAssets(skin);

        unitMenuAssets(skin);
        pauseMenuAssets(skin);
    }


    public static void mainMenuAssets(Skin skin){
        BitmapFont bfont = new BitmapFont();
        skin.add("default", bfont); //add font to skin

        //store the components of first menu
        skin.add("maps", new Texture(Gdx.files.internal("menu/mapsButton.png")));
        skin.add("settings", new Texture(Gdx.files.internal("menu/settingButton.png")));
        skin.add("factionButton", new Texture(Gdx.files.internal("menu/factionsButton.png")));

        //info panel for units when choosing units
        skin.add("infoPanel", new Texture(Gdx.files.internal("infopanel/mainpanel.png")));
    }



    public static void factionMenuAssets(Skin skin){
        //add faction menu Texture images
//        skin.add("humButton", new Texture(Gdx.files.internal("menu/humansButton.png")));
//        skin.add("artButton", new Texture(Gdx.files.internal("menu/artButton.png")));
//        skin.add("repButton", new Texture(Gdx.files.internal("menu/repButton.png")));

        ButtonStyle humStyle= new ButtonStyle();
        humStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/humansButton.png"))));
        skin.add("humStyle", humStyle);

        ButtonStyle repStyle = new ButtonStyle();
        repStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/repButton.png"))));
        skin.add("repStyle", repStyle);

        ButtonStyle artStyle = new ButtonStyle();
        artStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/artButton.png"))));
        skin.add("artStyle", artStyle);
    }


    public static void unitMenuAssets(Skin skin){
        /*** populates the skin with panel textures**
         *
         */
        //the Textures
        skin.add("infoPanel", new Texture(Gdx.files.internal("menu/units/infoUnits.png")));
        skin.add("smallPanel", new Texture(Gdx.files.internal("menu/units/smallUnits.png")));
        skin.add("medPanel", new Texture(Gdx.files.internal("menu/units/medUnits.png")));
        skin.add("largePanel", new Texture(Gdx.files.internal("menu/units/largeUnits.png")));
        skin.add("backBtn", new Texture(Gdx.files.internal("menu/backButton.png")));
        skin.add("dialog1", new Texture(Gdx.files.internal("menu/dialogBox.png")));
        skin.add("confirmBkgrnd", new Texture(Gdx.files.internal("menu/confirm1.png")));
        skin.add("counter", new Texture(Gdx.files.internal("menu/counterBox.png")));

        //shown on UnitImage actor; whether they are selected for viewing info
        skin.add("checkS", new Texture(Gdx.files.internal("menu/checkedUniSmall.png")));
        skin.add("checkM", new Texture(Gdx.files.internal("menu/checkedUniMed.png")));
        skin.add("checkL", new Texture(Gdx.files.internal("menu/checkedUniLarge.png")));
        Pixmap unitTableBckgrnd = AssetHelper.createPixmap((int)Constants.UNITS_TABLE_W, (int)Constants.UNITS_TABLE_H,
                Color.LIGHT_GRAY);
        skin.add("unitTableBack", new Texture(unitTableBckgrnd));

        //for adding units to roster for use in game play
        Pixmap selectedUnitsGrid = AssetHelper.createPixmap((int)Constants.UNIT_ROSTER_W, (int)Constants.UNIT_ROSTER_H,
                Color.LIGHT_GRAY);
        skin.add("unitRosterBack", new Texture(selectedUnitsGrid));


        //simple label style for unit types in menu
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("retro2");
//        labelStyle.font.scale(1.5f);
        labelStyle.fontColor = Color.OLIVE;
        skin.add("unitText", labelStyle);

        //button for popup dialog when choosing unit
        TextButtonStyle textStyle = new TextButtonStyle();
        textStyle.font = skin.getFont("default");
        skin.add("default", textStyle);

        ButtonStyle defaultBTStyle = new ButtonStyle();
        textStyle.font = skin.getFont("default");
        skin.add("default", defaultBTStyle);

        Window.WindowStyle confirmStyle = new Window.WindowStyle();
        confirmStyle.background = skin.getDrawable("confirmBkgrnd");
        confirmStyle.titleFont = skin.getFont("default");
        confirmStyle.titleFontColor = Color.BLACK;
        skin.add("dialogUnit", confirmStyle);		//added confirm style 1


    }


    public static void pauseMenuAssets(Skin skin){
        skin.add("pause-background", new Texture(Gdx.files.internal("menu/pause menu/pauseMenu-background.png")));
        skin.add("pause-music-slider", new Texture(Gdx.files.internal("menu/pause menu/pauseMenu-music-slider.png")));
        skin.add("pause-sounds-slider", new Texture(Gdx.files.internal("menu/pause menu/pauseMenu-sounds-slider.png")));
        skin.add("pause-slider-knob", new Texture(Gdx.files.internal("menu/pause menu/pauseMenu-sound-knob.png")));

        //sets the window style for pause menu
        Window.WindowStyle winStyle = new Window.WindowStyle();
        winStyle.titleFont = skin.getFont("default-small");
        winStyle.titleFont.scale(.01f); //scale it down a bit
        winStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/pause menu/pauseMenu-background.png"))));

        Slider.SliderStyle soundStyle = new Slider.SliderStyle();
        soundStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/textures/pauseMenu-sounds-slider.png"))));
        soundStyle.knob = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/textures/pauseMenu-sound-knob.png"))));

        Slider.SliderStyle musicStyle = new Slider.SliderStyle();
        musicStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("menu/textures/pauseMenu-music-slider.png"))));
        musicStyle.knob = soundStyle.knob;

        Label.LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("retro2");
        labelStyle.fontColor = Color.BLACK;

        skin.add("label-pause", labelStyle);
        skin.add("pause-window", winStyle);
        skin.add("slider-sound", soundStyle);
        skin.add("slider-music", musicStyle);
    }


    public static void loadOtherUI(Skin skin){
        //for game over image
        Pixmap gameOverBkgrnd = AssetHelper.createPixmap(100, 100, Color.MAGENTA);
        skin.add("gameOverBkgrnd", new Texture(gameOverBkgrnd));

        Pixmap startBkgrnd = AssetHelper.createPixmap(100, 100, Color.GREEN);
        skin.add("startBkgrnd", new Texture(startBkgrnd));

        //the score board label background texture & LabelStyle
        Pixmap scoreBoard = AssetHelper.createPixmap(64, 45, Color.LIGHT_GRAY);
        skin.add("scoreBkgrnd", new Texture(scoreBoard));
        LabelStyle scoreStyle = new LabelStyle();
        scoreStyle.background = skin.getDrawable("scoreBkgrnd");
        scoreStyle.font = skin.getFont("retro2");
        skin.add("scoreStyle", scoreStyle);

        //add a default confirm dialog
        Window.WindowStyle startDialog = new Window.WindowStyle();
        startDialog.background = skin.getDrawable("startBkgrnd");
        startDialog.titleFont = skin.getFont("retro1");
        startDialog.titleFontColor = Color.BLACK;
        skin.add("confirmStart", startDialog);

        //for unit health bar
        Pixmap healthbar = AssetHelper.createPixmap(Constants.HLTH_W, Constants.HLTH_H, Color.YELLOW);
        skin.add("healthBar", new Texture(healthbar));

    }


    //loads json-atlas-png format skins
    public static void setDarkSkin(){
        assetManager.load(Constants.DARK_SKIN, Skin.class);
    }

    //returns dark skin
    public static Skin getDarkSkin(){
        return assetManager.get(Constants.DARK_SKIN);
    }


    /**
	 * @return the assetManager
	 */
	public static AssetManager getAssetManager() {
		return assetManager;
	}


}
