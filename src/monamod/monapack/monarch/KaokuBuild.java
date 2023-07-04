package monarch;

import java.io.*;
import java.util.*;


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



//パネルゲーム用

public class KaokuBuild extends ImageView {

	static Pane p = new Pane();
	//static Group p;
	private int cntcb = 0;//ループ、アニメタイマー用

	//---------------------------------------------
	//ステータス
	int objNum = 2;//Chara = 1, Kaoku = 2
	String name;
	Image img;	//単体イメージ(viewportを使わない)
	String vid;//ImageViewのId(set,get)
	String path;//画像のパス
	String[][] paths;//
	int row, col;	//マス座標であって、xy座標ではない
	Point2D p2d;
	double hitP;	//体力
	Text hpTx;	//イメージに付随させる
	int team;	//チーム。今は2チーム
	double hohabaX, hohabaY;//歩幅。マス座標の変化を補正

	//---------------------------------------------
	//環境要因
	static int taxrate = 10;//税率。hagukumiに影響
	static double lpSpeed = 1;//CM,CB,KB共通


	static ArrayList<KaokuBuild> kaokulist = new ArrayList<KaokuBuild>();
	static HashMap<Point2D, Integer> kinsikuiki = new HashMap<Point2D, Integer>();

	//オリジナルクラス
	FieldMasu fl = new FieldMasu();
	CharaBuild cb;//生誕するキャラ
	CharaMansion cm;

	//=============================================
	//コンストラクタ
	public KaokuBuild() {}

	public KaokuBuild(/*Group p*/Pane p, String name, String[][] paths, int row, int col, int hitP, int team, CharaBuild cb) {
		//this.p = p;
		this.name = name;
		this.team = team;
		this.paths = paths;
		img = new Image(new File( paths[1][team-1] ).toURI().toString());
		setImage(img);
		this.setViewport(new Rectangle2D(0,0,32,32));
		this.path = path;
		this.row = row;
		this.col = col;
		p2d = new Point2D(row, col);
		this.hitP = hitP;
		hpTx = new Text(""+(int)hitP);//「""+」でStringにキャスト
		//hpTx = new Text(""+Math.round(hitP));//「""+」でStringにキャスト
		this.team = team;
		this.cb = cb;
		cm = new CharaMansion(p);
	}

	//=============================================
	//とりあえずある
	void print(Object obj, Object obj2) {
		System.out.println("  KAOKU BUILD  " + obj + obj2);
		System.out.println();
	}

	void print(Object obj) {//Overrode
		System.out.println("  KAOKU BUILD  " + obj);
		System.out.println();
	}




	//存在
	public void padd() {
		syokiiti();
		setVid();
		p.getChildren().add(this);
		p.getChildren().add(hpTx);
		inList(kaokulist, this);
		masuidou(fl.hakoW, fl.hakoW/4, fl.hakoW/8);
		fl.panelChange2(row, col, team, objNum, true);
		kinsikuiki.putIfAbsent(new Point2D(row,col), team);
	}

	void premove() {
		p.getChildren().remove(this);
		p.getChildren().remove(hpTx);
		fl.panelChange2(row, col, team, objNum, false);
	}

	void taijou() {//退場
		premove();
		kinsikuiki.remove(new Point2D(row, col));
		row = -1;
		col = -1;
		kaokulist.remove(this);
			System.out.println("KB " + name + " 死亡");
	}

	void sibou() {//死亡
		if((int)hitP <= 0 &&
			name != null
		) {
			taijou();
		}
	}

	void masuidou(double hakoW, double bicyouseiW, double bicyouseiH) {//hakoWは一マスの横幅
		this.setX(fl.kitenX - ( row * hakoW/2 ) + ( col * hakoW/2 ) + bicyouseiW);
		this.setY(fl.kitenY + ( col * hakoW/4 ) + ( row * hakoW/4 ) + bicyouseiH);
		hpTx.setX( this.getX() );
		hpTx.setY( this.getY() );
	}

	//kaokulistに入れる。唯一
	void inList(ArrayList<KaokuBuild> arr, KaokuBuild newKb) {//ダブり防止
		if( !arr.contains(newKb) ) {
			arr.add(newKb);
		}
	}

	void syokiiti() {//ゲームスタート時ランダムポジ
		int i = 0;
		while( fl.zeroList.contains(new Point2D(row, col)) ) {
			row = (int)(Math.random() * fl.flRow);
			col = (int)(Math.random() * fl.flCol);

			i++;
			if(i >= 1000) {break;}
		}
	}


	//=============================================
	//セッター

	void setHp(double h) {
		hitP = h;
		hpTx.setText(""+(int)hitP);
	}

	void setTax(int i) {
		taxrate = fl.ijouika((int)taxrate, i, 1, 30);
	}

	void setVid() {//paddより前にセット
		fl.vidcnt++;
		this.setId(""+ (fl.vidcnt + 100));
		hpTx.setId(""+ (fl.vidcnt + 100));
	}

	//=============================================
	//ギフター
	KaokuBuild copy(int r, int c) { //throws Exception使用する場合引数かっこの後に置き、実装側でtry-catchする
		KaokuBuild cpy = new KaokuBuild(p, name, paths, r, c, 100, team, cb);
		return cpy;
	}



	//=============================================
	//アクション

	void onLoop() {//ループ上で変化を監視

		hagukumi();
		sibou();

	}



	void hagukumi() {//キャラの生成
		double h = 0.4;

		if(taxrate >= 1) {//taxrate==0回避
			hitP = hitP + h / (1 + taxrate * lpSpeed / 7);//1+がないと数値の逆転が起きる
		} else {
			hitP = hitP + h;
		}
		setHp(hitP);

		if(hitP >= 150) {
				//fl.panelPrint();
			CharaBuild clcb = cb.copy(row, col);
			clcb.padd();
			cm.addCm(clcb);

			hitP = 50;
				//fl.panelPrint();
		}

	}



	void debug() {
		for(KaokuBuild kb : kaokulist) {
			System.out.println("KAOKU "+ kb.row +" "+ kb.col);
		}
	}
}

