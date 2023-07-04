package monarch;

import java.io.*;
import java.util.*;
import java.net.*;
//import java.awt.Font;


import javafx.animation.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.scene.text.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.util.*;
import javafx.beans.value.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.event.*;
import javafx.scene.control.skin.*;



//rectの色柄で配置の確認が取れる
//ｘｙ座標をマス座標col,rowに変換。
//追跡、当たり判定、移動制限、実装


public class Monarch extends Application {

	static Pane p = new Pane();
	//static Group p = new Group();
	int psiz;
	Scene scene;
	boolean isUp, isDown, isLeft, isRight, isD, isG, isB, isN, isX;
	protected boolean play = false;//ゲームの再生停止
	protected boolean bfPray = false;//playの状態を保持
	PerspectiveCamera camera = new PerspectiveCamera();
	//ParallelCamera camera = new ParallelCamera();
	double snX, snY;//mauseポインタがどこにあるか
	//試合終了
	private boolean reButai = true;//Gを押した時に自動連打にならないように
	Image keti;
	ImageView ketv;
	Comparator<Node> comp;
	boolean addSort = false;//addListenerでtrue化、loopでsortする
	
	//イベント用
	PickResult pickre;//選択対象の情報
	Node prNd;//pickre.getIntersectedNode()によって得られる。
	CharaBuild prCb;//(CharaBuild)prNd
	CharaBuild choiCb;//(CharaBuild)prCb.equals(aaaTeam(i));

	//色
	Color rcclr = new Color(0.5, 0.7, 0.5, 0.6);//範囲選択用（ドラッグイベント）

	int[][] panel = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,1,1,1,1,1,1,1,1,0},
			{0,1,1,0,1,1,1,1,1,0},
			{0,1,1,2,0,1,2,1,1,0},
			{0,1,1,1,1,0,1,1,1,0},
			{0,1,1,0,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,0},
			{0,0,0,0,0,0,0,0,0,0}
	};


	ClassLoader cl = this.getClass().getClassLoader();

/*	URL getUrl(String str) {
		URL url = cl.getResource(str);
		return url;
	}
*/

	URL getUrl(String str) {
		URL kari = null;
		try{
			kari = new File(str).toURI().toURL();
		} catch(Exception e) {
			
		}
		
		return kari;
	}

	String[] hakoPath2 = new String[3];
	String[] strCbArr2 = new String[6];
	String[] strKbArr2 = new String[3];

	String[] hakoPath4 = {//箱の絵
		getUrl("src/monamod/pictpack/nanamehakoao.png").toString(),
		new File("src/monamod/pictpack/nanamehako.png").toURI().toString(),
		new File("src/monamod/pictpack/nanamehakoaka.png").toURI().toString()
	};


	String[] hakoPath = {//箱の絵
		cl.getResource("nanamehakoao.png").toString(),
		cl.getResource("nanamehako.png").toString(),
		cl.getResource("nanamehakoaka.png").toString()
	};

	String[] strCbArr = {//キャラの絵
		cl.getResource("jurokuA32+3.png").toString(),
		cl.getResource("jurokuB32+3.png").toString(),
		cl.getResource("p128.png").toString(),
		cl.getResource("man04.png").toString(),
		cl.getResource("teki.png").toString(),
		cl.getResource("man05.png").toString()
	};

	String[] strKbArr = {//家屋の絵
		cl.getResource("kaoku01.png").toString(),
		cl.getResource("kaoku02.png").toString(),
		cl.getResource("kaoku03.png").toString()
	};

// -----------------------------------------
		String[] hakoPath3 = {//箱の絵
			"src/monamod/pictpack/nanamehakoao.png",
			"src/monamod/pictpack/nanamehako.png",
			"src/monamod/pictpack/nanamehakoaka.png"
		};
	
		String[] strCbArr3 = {//キャラの絵
			"src/monamod/pictpack/jurokuA32+3.png",
			"src/monamod/pictpack/jurokuB32+3.png",
			"src/monamod/pictpack/p128.png",
			"src/monamod/pictpack/man04.png",
			"src/monamod/pictpack/teki.png",
			"src/monamod/pictpack/man05.png"
		};
	
		String[] strKbArr3 = {//家屋の絵
			"src/monamod/pictpack/kaoku01.png",
			"src/monamod/pictpack/kaoku02.png",
			"src/monamod/pictpack/kaoku03.png"
		};
// ----------------------------------------


	String[][] strArr = {
		strCbArr,
		strKbArr
	};


	//キャラ選択時の選択肢-------------------------
	//mouseRele()にてshow()
	Rectangle choiRc;//選択範囲


	ContextMenu ctxmenu = new ContextMenu();//Menr1なに、キャラセレクトで始めに出る
	ContextMenu ctxmenu2 = new ContextMenu();//Menu2やめる、始めから選択アクションが終わるまで
	ContextMenu ctxmenu3 = new ContextMenu();//Menu3どこ
	ContextMenu ctxmenu4 = new ContextMenu();//Menu4だれ
	Menu menu1 = new Menu("なに？");//最初の質問
	Menu menu2 = new Menu("つくる");//

	//MenuItem[] menui = new MenuItem[9];//あと
	MenuItem[] menui = {
		new MenuItem(""),//[0]
		new MenuItem("やめる"),
		new MenuItem("どこ"),
		new MenuItem("だれ"),
		new MenuItem("待つ"),//[4]
		new MenuItem("いどう"),
		new MenuItem("追いかけ"),
		new MenuItem("こわす"),
		new MenuItem("おうち"),//[8]
		new MenuItem("はし"),
		new MenuItem("さく")//[10]
	};

	Popup ppup = new Popup();


	//---------------------------------------------


	//オリジナルクラス-------------------
	//一部はbutaiSetting()にある

		//マスセット
		FieldMasu fl = new FieldMasu(p, panel, hakoPath, 350, 50, 64.0); //引数（Group, panel, 箱の種類数, 起点XY, 箱のサイズ）


		//キャラセット
		CharaBuild cb = new CharaBuild();
		CharaBuild hero;
		CharaBuild teki;

		KaokuBuild kb = new KaokuBuild();
		KaokuBuild kaokuA;
		KaokuBuild kaokuB;

		Miji mi = new Miji();
		CharaMansion cm = new CharaMansion(p);
		
		HeadUpDisplay hud = new HeadUpDisplay();
		
		static CharaTeam[] cts = new CharaTeam[3];//チーム数＋１。０は使わないので

	//-----------------------------------

	//実験用
	static SubScene subs2;
	static Pane p2;

	//=======================================================
	//メインメソッド

	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage stage) throws Exception {

				//実験
				System.out.println(new File(hakoPath[0]).toURI().toURL());
				//

		scene = new Scene(p, 500, 500);
		stage.setScene(scene);
		stage.show();
		//カメラ
		scene.setCamera(camera);
		//subs1.setCamera(camera);

		//舞台設定
		butaiSetting();

			//試合終了時の準備
			keti = new Image(new File( "src/monamod/pictpack/kettyaku.png" ).toURI().toString());
			ketv = new ImageView( keti );
			ketv.setId(""+0);
			ketv.setX(100); ketv.setY(100);



		//キャラ選択時のメニュー〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜
		menu1.getItems().addAll(menui[4], menui[5], menui[6], menu2, menui[7]);//なに？の次
		menu2.getItems().addAll(menui[8], menui[9], menui[10]);//つくるの次

		ctxmenu.getItems().addAll(menu1);
		ctxmenu2.getItems().addAll(menui[1]);
		ctxmenu3.getItems().addAll(menui[2]);
		ctxmenu4.getItems().addAll(menui[3]);

			//イベントセット
			for(MenuItem item : menui) {
				item.setOnAction(e -> menuEve(e));
			}


		Text txt = new Text("POPUP");
		ppup.getContent().add(txt);
		//〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜〜

		//====================================================
		//最短距離アルゴリズム


		//====================================================
						//p.gC()の順番を変更してみる
						comp = new Comparator<Node>() {
							@Override
							public int compare(Node n1, Node n2) {
								//row,colを引き出す
								Integer in1 = cm.contaInt(cm.charalist, n1);
								Integer in2 = cm.contaInt(cm.charalist, n2);
								return Integer.compare(in1, in2);
							}
						};
						List<Node> mae = p.getChildren().sorted(comp);

						p.getChildren().setAll(mae);
						for(int i=0; i < mae.size(); i++) {
							//
						}


		//キーイベントの登録,,,,,,,,,,,,,,,,
		scene.setOnKeyPressed(e -> keyPressed(e));
		scene.setOnKeyReleased(e -> keyReleased(e));
		scene.setOnMouseClicked(e -> mouseC(e));
		//オブザーバブルリスト（監視可能リスト）
		p.getChildren().addListener( (ListChangeListener<Node>)evelis -> {//.............
			addSort = true;//リスナーの反応毎ではなく１ループに１回
			while( evelis.next() ) {//変更があれば起動する
				psiz = p.getChildren().size();

				if( evelis.wasAdded() ) {//追加があれば
					for(int i=0; i < psiz; i++) {
						final int j = i;
							//操作対象にする
						if(p.getChildren().get(j).getId() != null ) {

						}
					}
				}

				for(int i=0; i < psiz; i++) {
					if(p.getChildren().get(i).getId() == "0") {
						//zind = j;
					}
				}
			}
		});//....................................................
		//スクロール（マウスホイール）イベント
		scene.setOnScroll(e -> scroll(e));
		scene.setOnMouseDragged(e -> mouseDrag(e));
		scene.setOnMousePressed(e -> mousePrDr(e));
		scene.setOnMouseReleased(e -> mouseRele(e/*, stage*/));
		scene.setOnMouseMoved(e -> mouseMove(e));

			//（実験中）コンテキストメニューが呼び出されるとき発生するイベント
			//呼び出し自体はKeyやMouseで呼び出すので、いまいち使いどころがよくわからん
				//scene.setOnContextMenuRequested(e -> menuEve(e));

		//,,,,,,,,,,,,,,,,,,,,,



				//実験----------------------
				jikken();


				//--------------------------


		//ゲームループの起動。必須
		new AnimationTimer() {

			@Override
			public void handle(long arg0) {
				gameLoop();
			}

		}.start();

	}//start,end=================================================================


	//ゲームループ。必要
	private void gameLoop() {
		jikkeninloop();
		player(isB, isN);
		choiceWait();
		hudInLoop();

		if(play) {
			counter();
			noCount();

			cm.loopSpeed(isD, isX);
		} else {

		}
	}//gameloop,end


	private void noCount() {
		//全滅したら
		if( !cm.teamEnpty() ) {
			cm.action();
			for(int i=1; i < cts.length; i++) {
				cts[i].action();
			}
		} else {
			System.out.println("SYU-RYO!");

			p.getChildren().add(ketv);//終了表示

			play = false;
		}

		if(addSort) {
			List<Node> mae2 = p.getChildren().sorted(comp);
			p.getChildren().setAll(mae2);
			//p.getChildren().sort(comp);
			addSort = false;
		}
		
	}

	//時間差処理用。要る
	int cnt = 0;
	void counter() {
		cnt++;

		if(cnt >= 60) {
			cnt = 0;
		} else if(cnt >= 1000) {
			cnt = 0;
		}
	}


	//fx.Application内でThreadは使いたくない
	protected boolean choiWait = false;//入力（選択）要求中
	void choiceWait() {//どこ？入力待ち。playをコントロール
		if(choiWait) {
			
			hud.p.setMouseTransparent(true);
			cb.p.setMouseTransparent(true);
			kb.p.setMouseTransparent(true);

			if(pickre != null && choiCb != null) {
				//choiCb.tgtPd = 
				
				choiWait = false;
				play = bfPray;
			}
			
		} else {
			
			hud.p.setMouseTransparent(false);
			cb.p.setMouseTransparent(false);
			kb.p.setMouseTransparent(false);
			
		}
	}


	//再生停止
	public void player(boolean isB, boolean isN) {
		
		if( !choiWait ) {
			if( isB ) { 
				play = false; 
			} else {}
			
			if( isN ) {
				if(!cm.teamEnpty()) {
					play = true; 
				}
			} else {}
		}
		
		if( isG ) {
			if(reButai) {//キーを押した時に舞台設置を連続させない
				reButai = false;
				play = false;
				p.getChildren().clear();//終了表示
				cb.p.getChildren().clear();
				kb.p.getChildren().clear();
				fl.p.getChildren().clear();
				hud.p.getChildren().clear();
				try {
					p.wait(5000);//終了表示
				} catch(Exception e) {}
				cm.zenkesi();
				butaiSetting();
			}
		 } else {
		 	reButai = true;
		 }
	}

	private void butaiSetting() {
		//マスセット
		fl.action();
		mi = new Miji();
		cm = new CharaMansion(p);

			print("CTS length", cts.length);
			for(int i=0; i < cts.length; i++) {
				cts[i] = new CharaTeam(i, 1000);//(team, money)
			}

		//キャラをGroupに加入。ある時期から各クラスのPaneに格納し、そのPaneごとメインクラスのParentに格納している。
		hero = new CharaBuild(p, "HERO", strArr, (int)fl.sndPds[0].getX(), (int)fl.sndPds[0].getY(), 300, 1, cts[1]);
		teki = new CharaBuild(p, "TEKI", strArr, (int)fl.sndPds[1].getX(), (int)fl.sndPds[1].getY(), 100, 2, cts[2]);
		kaokuA = new KaokuBuild(p, "HOME", strArr, (int)fl.sndPds[2].getX(), (int)fl.sndPds[2].getY(), 90, 1, hero);
		kaokuB = new KaokuBuild(p, "HOUSE", strArr, (int)fl.sndPds[3].getX(), (int)fl.sndPds[3].getY(), 90, 2, teki);

		hero.padd();
		teki.padd();
		kaokuA.padd();
		kaokuB.padd();
		
		p.getChildren().add(fl.p);
		p.getChildren().add(kb.p);
		p.getChildren().add(cb.p);


		cm.addCm(hero,teki);

		hero.setKaoku(kaokuA);
 		teki.setKaoku(kaokuB);


		//範囲選択
		choiRc = new Rectangle();
		choiRc.setFill(rcclr);
		choiRc.setId(""+10000);
		p.getChildren().add(choiRc);


			//「どこ？」
			selLb.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
			selLb.setScaleY(0.0);
			//selLb.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30));
			selLb.setFont(Font.font("Menlo"));
			selLb.setText("abcd1lIi");
			p.getChildren().add(selLb);
		
		
		//HUD。メインクラスフィールドのhudとは違うインスタンスになる
		HeadUpDisplay hud = new HeadUpDisplay();
		//hudActJikken(hud);
		hud.action();
		//hudSetJikken(hud);
		hud.setTime(0, cb.lptim());
		hud.setTime(1, kb.taxrate);
		cb.lptimBase = (int)hud.slidCb.getValue();
		kb.taxrate = (int)hud.slidKb.getValue();
		cm.lpSpeed = (int)hud.slidLs.getValue();
		p.getChildren().add(hud.p);




	}


	//HUDinLoop
	private void hudInLoop() {
		
		//hudクラスでメソッドを作ってメインに置いたが、slidCbとslidKbの反応がテレコになるのでここでそれぞれのif文を作った
		//if( hud.slidCb.isValueChanging() ) {//ドラッグでは効果があるがクリックでは効かない
		if(hud.slidCb.getValue() != cb.lptim() && hud.slidCb.getValue() % 1 == 0) {
			print("SLIDER[0] VALUE", hud.slidCb.getValue());
			//cb.lptimBase = (int)hud.slidCb.getValue();
			hud.setTime(0, cb.lptim());
		}
		
		//if( hud.slid[1].isValueChanging() ) {//ドラッグでは効果があるがクリックでは効かない
		if(hud.slidKb.getValue() != kb.taxrate && hud.slidKb.getValue() % 1 == 0) {
			print("SLIDER[1] VALUE", hud.slidKb.getValue());
			kb.taxrate = (int)hud.slidKb.getValue();
			hud.setTime(1, kb.taxrate);
		}
		
		if(hud.slidLs.getValue() != cm.lpSpeed  &&  hud.slidLs.getValue() % 0.5 == 0) {

				//slidLsと他のスライダを連動
				if(hud.slidLs.getValue() > cm.lpSpeed) {
					hud.slidCb.increment();//BlockIncrement分移動。eventなどで使う
				} else {
					hud.slidCb.decrement();
				}
				
			print("SLIDER[2] VALUE", hud.slidLs.getValue());
			//cm.lpSpeed = (int)hud.slidLs.getValue();
			cm.loopScoop(hud.slidLs.getValue());
			hud.setTime(2, cm.lpSpeed);
		}

	}






	//イベント＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝


	//カメラの操作.................................
		double gsx, gsy, gfx, gfy;//start,final
	private void mousePrDr(MouseEvent e) {//mousePressed
		//マウスドラッグ開始位置
		gsx = 0;
		gsy = 0;
		gsx = e.getX();//シーン上の座標
		gsy = e.getY();
		print("MOUSEPRESS  ", e.getX());
	}


	LinkedHashSet<CharaBuild> selChaSet = new LinkedHashSet<CharaBuild>();//範囲選択choiRcに触るもの
	private void mouseDrag(MouseEvent e) {

		//scene.startFullDrag();
		gfx = e.getX();
		gfy = e.getY();

		if(e.isPrimaryButtonDown()) {//プライマリ（左）
			//double settx = camera.getTranslateX() - gfx + gsx;
			camera.setTranslateX(camera.getTranslateX() - gfx + gsx);
			camera.setTranslateY(camera.getTranslateY() - gfy + gsy);

			hud.p.setTranslateX(camera.getBoundsInParent().getMinX() + 5);
			hud.p.setTranslateY(camera.getBoundsInParent().getMinY() + 5);
		}


		//キャラ範囲選択
		if(e.isSecondaryButtonDown()) {//セカンダリ
			if( (gfx-gsx) < 0 ) {//左にドラッグ
				choiRc.setX(gfx);
				choiRc.setWidth(gsx-gfx);

				if( (gfy-gsy) < 0 ) {//左かつ上にドラッグ
					choiRc.setY(gfy);
					choiRc.setHeight(gsy-gfy);
				} else {//左かつ下にドラッグ
					choiRc.setY(gsy);
					choiRc.setHeight(gfy-gsy);
				}

			} else if( (gfx-gsx) > 0 ) {//右にドラッグ
				choiRc.setX(gsx);
				choiRc.setWidth(gfx-gsx);

				if( (gfy-gsy) < 0 ) {//右かつ上にドラッグ
					choiRc.setY(gfy);
					choiRc.setHeight(gsy-gfy);
				} else {//右かつ下にドラッグ
					choiRc.setY(gsy);
					choiRc.setHeight(gfy-gsy);
				}
			}

			if( e.getPickResult().getIntersectedNode() != null &&
				e.getPickResult().getIntersectedNode().getClass().getName() == "CharaBuild"
				//e.getPickResult().getIntersectedNode() instanceof CharaBuild
			 ) {
				print("     CharaBuild dane ");

			}

		}
	}


	//マウスホイールイベント
	private void scroll(ScrollEvent e) {
			snX = e.getSceneX();//シーン上の座標
			snY = e.getSceneY();
		double bai = 0.10;//１スクロールの拡大倍率
		double d = camera.getScaleY() - ( bai * Math.signum(e.getDeltaY()) );//e.getDeltaはYのみ
		double tz = camera.getTranslateZ() - (20 * Math.signum(e.getDeltaY()) );
		camera.setTranslateZ(tz);
	}
	//.............................................



	double kariX = 0;//ContextMenu.show用の座標記憶
	double kariY = 0;//
	private void mouseRele(MouseEvent e/*, Stage stage*/) {//contexMenuのshow()でstageが要る?

		ctxHide();

		//choiRc境界矩形に触れた状態でマウスを放すと
		for(int i=0; i < cm.aaTeam.size(); i++) {//hero.teamを選別
			if(choiRc.intersects(cm.aaTeam.get(i).getBoundsInLocal())) {//----------->> これもしかしたらPickResultで簡略できるかも
				selChaSet.add(cm.aaTeam.get(i));
				System.out.println("     OOATARI ");

				kariX = cm.aaTeam.get(i).getX();
				kariY = cm.aaTeam.get(i).getY()+30;
					//ctxmenu.setAutoHide(true);
				//ctxmenu.show(stage, e.getScreenX(), e.getScreenY());
				ctxmenu.show(p, Side.LEFT, kariX, kariY);
				ctxmenu2.show(p, Side.LEFT, kariX+60, kariY-50);
			}
		}

		choiRc.setWidth(0);
		choiRc.setHeight(0);

		//選択されたキャラにエフェクトをかける
		for(CharaBuild cb : selChaSet) {
			cb.setEffect(new DropShadow(10, Color.WHITE));
			print(1, "  effect");
		}

		selChaSet.clear();

	}


	Label selLb = new Label("べろべろばー");
	private void menuEve(ActionEvent e) {//選択肢を選んだ時

		print(1, e.getSource().toString() );

		for(CharaBuild kariCb : cm.aaTeam) {
			if(prNd.equals(kariCb)) {
				choiCb = kariCb;

				if(e.getSource() == menui[1]) {//やめる
					print("やめるよ", 1);
					choiWait = false;
					play = bfPray;
				} else {
					//ここに↓このメソッドを置けば「いどう」などのif処理内に置かなくて良い。
					//waitやsleepなどを使っていないので、この処理の後はplayer()やchoiceWait()に頼る
					dokoChoice();//ifの外に置いた場合、こわす→やめるをした時bfPray==falseになってしまう
				}
				
				
				if(e.getSource() == menui[4]) {//待つ
					print("待ってるよ", 4);
					//cb.nanisuru = 't';//PickResultとcm.cbが連携できればいいのに
					//待つ、はその場に留まるだけなのでchoiceが無い
					choiWait = false;
					play = bfPray;
					
					kariCb.nanisuru = 't';
					
				} else if(e.getSource() == menui[5]) {//いどう
					selLbShow("どこ", kariX, kariY, 1.0);
					
					kariCb.nanisuru = 'i';
				} else if(e.getSource() == menui[6]) {//追いかけ
					selLbShow("だれ", kariX, kariY, 1.0);
					kariCb.nanisuru = 'o';
				} else if(e.getSource() == menui[8] || e.getSource() == menui[9] || e.getSource() == menui[10]) {//つくる
					selLbShow("どこ", kariX, kariY, 1.0);
					kariCb.nanisuru = 's';
				} else if(e.getSource() == menui[7]) {//こわす
					selLbShow("どれ", kariX, kariY, 1.0);
					kariCb.nanisuru = 'h';
				}
				
				
			}//if,end(prNd)
		}//for,end
		
		
		pickre = null;
		ctxHide();
		
	}
	

	void dokoChoice() {//どこに行くか選択するまでplayを停止
			print("dokoChoice  START");

		//bfPray = false;
		choiWait = true;//
		//pickre = null;//menuEveに預ける
		
		if(play) {//playの状態を保持
			bfPray = true;
			play = false;
		} else {
			bfPray = false;
		}
		
		//play = bfPray;
		print("dokoChoice  END");

	}


	private void mouseC(MouseEvent e) {
		
		choiceWait();

		pickre = e.getPickResult();//選択対象の情報
		prNd = pickre.getIntersectedNode();
		kariX = pickre.getIntersectedPoint().getX();
		kariY = pickre.getIntersectedPoint().getY();
		
		
		if(choiWait) {
			HakoView kariHako = (HakoView)prNd;
			print("MOUSE C ", kariHako.row);
			print("MOUSE C ", pickre.getIntersectedPoint());
			
			choiCb.tgtPd = new Point2D(kariHako.row, kariHako.col);
		} else {}
		
		
		
		//情報開示
		if(reType(prNd).equals("HakoView")) {//Nodeのタイプが___ならば
			print("  HAKOVIEW  ",prNd);
		} else if(reType(prNd).equals("CharaBuild")) {
			
			CharaBuild kariCb2 = (CharaBuild)prNd;
			print("情報開示==============", kariCb2);
			print(kariCb2.nanisuru, kariCb2.tgtPd);
			print(kariCb2.taiCb);
			print("===================");
			
			
			for(CharaBuild kariCb : cm.aaTeam) {
				if(prNd.equals(kariCb)) {
					choiCb = (CharaBuild)kariCb;
					
				}
			}
			
			
		} else {
			print("  NOT HAKO  ",prNd);
		}

		

		//コンテキストメニュー
		for(int i=0; i < cm.aaTeam.size(); i++) {//hero.teamを選別
			if( cm.aaTeam.get(i).equals(prNd) ) {
					//ctxmenu.setAutoHide(true);
				//ctxmenu.show(stage, e.getScreenX(), e.getScreenY());
				ctxmenu.show(p, e.getScreenX(), e.getScreenY());
				//ctxmenu.setOnAction(ae -> menuEve(ae));
				ctxmenu2.show(p, Side.LEFT, kariX+60, kariY-50);
			}
		}

	}

	private void mouseMove(MouseEvent e) {
		PickResult pickre2;
		Node prNd2;
		
		
		/*
		if(choiWait) {
			pickre2 = e.getPickResult();//選択対象の情報
			prNd2 = pickre2.getIntersectedNode();
			
			print(pickre2);
			print(hud.p);
			
			if(reType(prNd2).equals("Pane")) {
			//if(prNd2.equals(hud.p)) {
				print("MOUSE MOVED  ", "HUD.P");

				//hud.p.setMouseTransparent(true);

			} else {}
			
			if(reType(prNd2).equals("Pane")) {
				//cb.p.setMouseTransparent(true);
			} else {
				//hud.p.setMouseTransparent(false);
			}
			
			
			if(reType(prNd2).equals("Pane")) {
				//kb.p.setMouseTransparent(true);
			} else {
				//hud.p.setMouseTransparent(false);
				//cb.p.setMouseTransparent(false);
			}
			
		} else {
			
			
			hud.p.setMouseTransparent(false);
			cb.p.setMouseTransparent(false);
			kb.p.setMouseTransparent(false);
			
		}
		
		*/

		
	}


	private void mousePressed(MouseEvent e, int i) {

	}

	private void ctxHide() {
		ctxmenu.hide();//多重開示を回避
		ctxmenu2.hide();//やめる
		ctxmenu3.hide();
		ctxmenu4.hide();
	}
	
	
	private void selLbShow(String str, double x, double y, double h) {
		selLb.setText( str );
		selLb.setLayoutX( x );
		selLb.setLayoutY( y ); 
		selLb.setScaleY( h );
	}
	
	
	//------------------------------------------
	
	private void print(Object obj, Object obj2) {
		System.out.println("  MONARCH  " + obj + obj2);
		System.out.println();
	}

	private void print(Object obj) {//Overrode
		System.out.println("  MONARCH  " + obj);
		System.out.println();
	}

	String reType(Object obj) {//型名を調べる
		return obj.getClass().getSimpleName();
	}


	//キーを押した時のイベント
	private void keyPressed(KeyEvent e) {
		//上下左右キーを押した時フラグをONにする。
		switch(e.getCode()) {
		case LEFT:
			isLeft = true;
			break;
		case RIGHT:
			isRight = true;
			break;
		case UP:
			isUp = true;
			break;
		case DOWN:
			isDown = true;
			break;
		case D:
			isD = true;
			break;
		case G:
			isG = true;
			break;
		case B:
			isB = true;
			break;
		case N:
			isN = true;
			break;
		case X:
			isX = true;
			break;
		default:
			break;
		}
	}//keypres,end
 
	//キーを離した時のイベント
	private void keyReleased(KeyEvent e) {
		//上下左右キーを離した時フラグをOFFにする。
		switch(e.getCode()) {
		case LEFT:
			isLeft = false;
			break;
		case RIGHT:
			isRight = false;
			break;
		case UP:
			isUp = false;
			break;
		case DOWN:
			isDown = false;
			break;
		case D:
			isD = false;
			break;
		case G:
			isG = false;
			break;
		case B:
			isB = false;
			break;
		case N:
			isN = false;
			break;
		case X:
			isX = false;
			break;
		default:
			break;
		}
	}//keyrer,end

	//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	double rd(double d) {
		return (double)Math.round(d*1000)/1000;
		//return (double)Math.round(d*10)/10;
	}


				//実験===============================
					//実験用メンバ
					
				
				void jikken() {

				}

				void jikkeninloop() {

					//sliderでスピードを変更ーーーーーーーー
					/*
						cb.lptim() = hud.changeSlidVal(hud.slidCb, cb.lptim());
						hud.setTime(0, cb.lptim());
						kb.taxrate = hud.changeSlidVal(hud.slidKb, kb.taxrate);
						hud.setTime(1, kb.taxrate);
					*/
					
					
					

					
					
					//ーーーーーーーーーーーーーーーーーーーーーー
					
					//hud.slidによる変更実験ができれば消す
					if(isD) {
						//hudSetJikken(hud);
						hud.setTime(0, cb.lptim());
						hud.setTime(1, kb.taxrate);

							hud.p.setMouseTransparent(true);
							cb.p.setMouseTransparent(true);
							kb.p.setMouseTransparent(true);

					}
					
					if(isX) {
						//hudSetJikken(hud);
						hud.setTime(0, cb.lptim());
						hud.setTime(1, kb.taxrate);

							hud.p.setMouseTransparent(false);
							cb.p.setMouseTransparent(false);
							kb.p.setMouseTransparent(false);

					}

				}


					//実験のためのメソッド
					void conMenuEve(ContextMenuEvent e) {
						print("jikken", "100");
					}

					void ie(ActionEvent e) {
							print("jikken", "100");
							ppup.show(p, 100, 100);
					}
					
					void hudActJikken(HeadUpDisplay hu) {
						hu.action();
					}
					
					void hudSetJikken(HeadUpDisplay hu) {
						//hu.setTime(cb.lptim(), kb.taxrate);
					}
				//===================================

}//class,end



/*
//メモ

	オブジェクトのIDは、
		地面マスやテキストは０
		家屋など固定されたのは　１０＋通し番号
		可動キャラは　１００＋通し番号
	になっている。各生成クラスのsetId()などで確認する。

*/

/*
			//p.gC()の順番を変更してみる
			Comparator<Node> comp = new Comparator<Node>() {
				@Override
				public int compare(Node n1, Node n2) {
					int p1 = Integer.parseInt( n1.getId() );
					int p2 = Integer.parseInt( n2.getId() );
					return Integer.valueOf(p1).compareTo( Integer.valueOf(p2) );
				}
			};
			//List<Node> mae = p.getChildren();
			p.getChildren().sort(comp);

			//List<Node> ato = p.getChildren().sorted(comp);
			//List<Node> ato = p.getChildren();
				System.out.println("NARABIKAE  mae " + mae.get(0).getId() +"  ato "+ p.getChildren().get(0).getId());

			for(int i=0; i < mae.size(); i++) {
				//System.out.println("NARABIKAE  mae " + mae.get(i).getId() +"  ato "+ p.getChildren().get(i).getId());
			}
				System.out.println(" " );System.out.println(" " );
*/