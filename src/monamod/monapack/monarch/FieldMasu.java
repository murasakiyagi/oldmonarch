package monarch;

import java.io.*;
import java.util.*;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.Point2D;

//マスゲームの舞台を設定するクラス
//および各クラス各メソッドで使う面倒な処理をここに一括する
/*処理の順番
	舞台の広さをランダムで決定。autoField()
	０マス、１マスを決める。autoField()
		マスの属性を示すpanel[][]インスタンスのみ。autoField()
	zeroListの生成、panel[][]に０マス、１マスを決定。zeromasu(),syokiiti()
	初期配置を決定。walkSetに追加。syokiiti()
	初期配置(fstPds[fs])から別の初期配置(fstPds[fs+1])までランダムに歩き回り、walkSetに追加していく。keepOn(),syokiiti()
	上記の処理が出来ているかチェック。syokiiti()
	歩けるマスと０マスの比率、初期配置間の連絡が不合格なら始めからやり直し。keepOut()

*/

public class FieldMasu {

	static Pane p = new Pane();
	//static Group p;
	static int vidcnt = 0;//pに含まれるNodeの通し番号

	//プロパティ
		//fieldは一応ここに参考に書くが、メインで作成して、コンストラクタで上書きする
	static int[][] field = {//int[9][10],0の数15+34=49
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

	//rowとcolumnの関係。row=y, column=x
	static int flRow = field.length;	//行数。横並びが何個あるか
	static int flCol = field[ field.length-1 ].length;	//列数。縦並びが何個あるか
	static int kitenX;//マス群の描画起点。本当はdouble。XはFieldSizeによってはSceneからはみ出すのでauteField()内で式を組んでいる。だからコンストラクタで引数は必要ない
	static int kitenY;//Yは任意

	//Image と ImageViewの違いは、ファイルを読み込むクラスと、読み込んだデータを描画するクラス
	Image[] hako;	//箱のイメージ
	String[] hakoPath;	//hakoの画像のファイルネーム、パス
	HakoView hakoView; //メソッド内でインスタンス
	//Node hakoView;
	static double hakoW, hakoH; //箱の大きさ
	static double bicyouseiW, bicyouseiH;//微調整

	int row = 0;//Field[row][col]
	int col = 0;
	public static ArrayList<Point2D> zeroList = new ArrayList<Point2D>();//fieldの0のマスだけ集める
	private int zeroSize;//zeroListの長さ
	private HashSet<Point2D> walkSet = new HashSet<Point2D>();//歩ける場所。keepOn(),syokiiti()。初期位置から別の初期位置を探し回り歩いた場所を追加
	
	
	//パネルの状態
	public static int[][] panel = new int[flRow][flCol];//fieldの中の数値をいじるため
	public static Paneler[][] panels = new Paneler[flRow][flCol];//fieldの中の数値をいじるため
	public boolean[][] panelJi = new boolean[flRow][flCol];//地面か
	public boolean[][] panelCb = new boolean[flRow][flCol];//キャラは居るか
	public boolean[][] panelKb = new boolean[flRow][flCol];//味方の家はあるか


	//in syokiiti()
	Point2D fstPds[] = new Point2D[4];
	Point2D sndPds[] = new Point2D[fstPds.length];
	Boolean[] fstbo = new Boolean[fstPds.length];//始点がすべて通じているか。
	int fs;//fstPds[fs]
	//Point2D fstPd, sndPd, serPd, forPd;//キャラ初期位置。ここから進入できないマスは0にする


	//ラストチェック
	int masuSize;//Fieldの全マスの数 flRow * flCol
	int noAdmit;//立ち入り禁止のマス数 masuSize - walkSet.size()
	int flDivide;//分配。( flRow * flCol - (noAdmit + walkSet.size()) == 0 )
	boolean checkPass = false;
	
	
	//コンストラクタ=========================================

	public FieldMasu() {}; //スーパークラスを(があれば)そのままインスタンスするやつ

	public FieldMasu(/*Group p*/Pane p, int[][] field, String[] hakoPath, int x, int y, double hakosize) {
		//this.p = p;//static Group p;メインで宣言

			//this.field = field;

		this.hakoPath = hakoPath;
		hako = new Image[ hakoPath.length ];	//箱の種類数
		flRow = field.length;	//行数。横線が何個あるか
		flCol = field[0].length;	//列数。縦線が何個あるか
		kitenX = x; //マスを並べるときの初めの座標。Xでありrowでない
		kitenY = y; //Yでありcolでない
		hakoW = hakosize;
		bicyouseiW = hakoW / 4;
		bicyouseiH = hakoW / 8;
	}

	//=======================================================
	//メインアクション---------
	public void action() {
		vidcnt = 0;
		lastCheck();
		//walkSet.clear(); keepOut();
		hakoire();
		masuNarabe();
	}
	//-------------------------


	private void lastCheck() {//最終チェック。action
		int fccnt = 0;
		walkSet.clear();//初期化。lastCheck()は全ての処理の始めに来るので、下のwhileには必ず一回引っかかる

		while( (flRow * flCol) != (zeroList.size() + walkSet.size()) ) {//全マス数 != ０マス＋歩けるマス、であればやり直し
			walkSet.clear();
			keepOut();

			fccnt++;
			if(fccnt >= 100) {//無限ループ防止
				fccnt = 0;
				print(" LAST_CHECK  無限ループ防止");
				break;
			}
		}
	}


	//autoField()を成功させる
	private void keepOut() {//lastCheck

		int i = 0;
		int jj = 0;
		while( (flRow * flCol / 3) > walkSet.size() ) {//歩けるマスとゼロマスの比率
		
			autoField();//とりあえず作ってみる
			
			while( Arrays.asList(fstbo).contains(false) ) {//初期配置同士の連絡ができるか。fstboにfalseがあれば最初からやり直し
				jj++;
				autoField();
			}
		}

		//autoFieldが全部終わったら、fieldの値を修正
		for(int k=0; k < flRow-1; k++) {
			for(int j=0; j < flCol-1; j++) {
				if(walkSet.contains(new Point2D(k, j))) {//walkSet=通れるマス=１のマス、でないなら０に。２マスも１に変わる
					field[k][j] = 1;
				} else {
					field[k][j] = 0;
				}
			}
		}

		zeromasu();//fieldの値からzeroListを作り直すだけ
	}


	private void autoField() {//keepOut

		//fieldの基礎
		//field = new int[8][8];//実験用
		field = new int[ (int)(Math.random() * 10 + 6) ][ (int)(Math.random() * 10 + 6) ];//ランダム生成
		flRow = field.length;	//行数。横線が何個あるか
		flCol = field[0].length;	//列数。縦線が何個あるか
		kitenX = (int)( flRow * hakoW/2 + 10 );//描画位置の調整

		//障害物(０マス)生成
		for(int i=0; i < flRow-1; i++) {
			//周囲を０で囲む
			Arrays.fill(field[i], 1);//その行の全要素に 1 を格納
			for(int j=0; j < flCol-1; j++) {
				field[0][j] = 0;//上端の行
				field[ flRow-1 ][j] = 0;//下端の行

				//障害物生成
				if(Math.random() < 0.2) {//数値が1.0に近いほど０マスが多くなる
					field[i][j] = 0;
				}

			}
			field[i][0] = 0;//左端の列
			field[i][ flCol-1 ] = 0;//右端の列
		}

		panel = new int[flRow][flCol];
		panels = new Paneler[flRow][flCol];

		syokiiti();

	}//autoField.end--------------------------



	private void syokiiti() {//autoField
		//main舞台設定時、キャラの初期配置

		zeromasu();
		//lastCheck引っかかり、やり直しの時のエラー対策----
		for(int i=0; i < fstPds.length; i++) {
			//Point2D[4] fstPds:キャラ初期位置
			fstPds[i] = null;
			sndPds[i] = null;
		}
		fs = 0;
		//----------------------------

		int bkcnt = 0;//無限ループ防止

		while( Arrays.asList(fstPds).contains(null) ) {//すべてにnullがなくなるまで
			row = (int)(Math.random() * flRow);
			col = (int)(Math.random() * flCol);

			if( !zeroList.contains(new Point2D(row, col)) &&
				!Arrays.asList(fstPds).contains(new Point2D(row, col)) &&
				fstPds[fs] == null
			) {//ゼロマスでない。他と同じ場所でない
				//初期配置
				fstPds[fs] = new Point2D(row, col);
				sndPds[fs] = new Point2D(row, col);
				field[row][col] = 2;
				
				fs++;
				
			} else {}

			bkcnt++;
			if(bkcnt >= flRow * flCol * 50) {
				System.out.println("SYOKIITI BREAK 1 ");
				break; 
			}//無限ループ防止保険
		}

		//初期配置を歩けるマスリストに格納
		for(int k=0; k < fstPds.length; k++) {
			walkSet.add(fstPds[k]);
			fstbo[k] = false;//初期化
		}


		//各初期配置からスタートしkeepOnにより適当に歩き回り、別の初期位置まで行く
		//その際、歩けるマスリストwalkSetに格納していく
		//０マスによって孤立しないように
		for(int j=0; j < fstPds.length; j++) {

			bkcnt = 0;
			
			//現在(sndPds == fstPds)である。
			//keepOn(Point2D)によって(sndPds != fstPds)になるが
			//sndPds[0] == fstPds[max]
			//sndPds[1] == fstPds[0]
			//sndPds[2] == fstPds[1]、となるように処理する
			while(fstbo[j] == false) {//すべてtrueになるまで
				bkcnt++;

				fstPds[j] = keepOn(fstPds[j]);

				//fstPdsの各POSが他のPOSまで行けるように
				if(j == fstPds.length-1) {//fstPds[max]
					if( sndPds[0].equals(fstPds[j]) ) {//sndPds[0].equals(fstPds[max])
						fstbo[j] = true;
					}
				} else if( sndPds[j+1].equals(fstPds[j]) ) {//
					fstbo[j] = true; 
				} else {
					fstbo[j] = false; 
				}


				if(bkcnt >= flRow * flCol * 50) {
					System.out.println("SYOKIITI BREAK 2 ");
					break; 
				}//無限ループ防止保険。fstboが全てtrueでなければ別メソッドで繰り返される

			}

		}
	}


	private Point2D keepOn(Point2D pd) {//syokiiti
		//walkSet移動可能な場所のリストに加え、次の位置ポイントを返す
		
		int pr = (int)pd.getX();
		int pc = (int)pd.getY();
		int rdm1 = (int)(Math.random() * 3) - 1;//-1,0,1
		int rdm2 = (int)(Math.random() * 3) - 1;

		if(rdm1 != 0) {//row移動

			if( field[ pr + rdm1 ][ pc ] >= 1 ) {//マスの属性が０でない
				pd = pd.add(rdm1, 0);
				walkSet.add(pd);
			}

		} else {//col移動。rdm1が０である

			if( rdm2 != 0 &&
				field[ pr ][ pc + rdm2 ] >= 1
			) {
				pd = pd.add(0, rdm2);
				walkSet.add(pd);
			}

		}
		return pd;//rdm1,rdm2共に０なら何も変化なしで引数のpdを返す
	}


	private void zeromasu() {//フィールドの０のマス。zeroListとzeroSize,panel[][]の属性を決める
		zeroList.clear();//このメソッドに含むか迷う

		for(int k=0; k < flRow; k++) {
			for(int j=0; j < flCol; j++) {
				if(field[k][j] == 0) {
					zeroList.add( new Point2D(k,j) );
					panel[k][j] = 0;
					panels[k][j] = new Paneler(0);
				} else {
					panel[k][j] = 1;
					panels[k][j] = new Paneler(1);
				}
			}
		}

		zeroSize = zeroList.size();
	}


	//描画準備ーーーーーーーーーーーーーーーー

	private void hakoire() {//action
		//hakoPath[i]の内容（パス）はメインで一々設定する。
			//例　fl.hakoPath[0] = "../img/karahako.png";
		for(int i=0; i < hako.length; i++) {
// 			hako[i] = new Image(new File( hakoPath[i] ).toURI().toString());
			hako[i] = new Image(hakoPath[i]);//url.toString()はFileを使わない
		}
		hakoW = hako[0].getWidth();//箱のサイズは全部同じだからhako[0]
		hakoH = hako[0].getHeight();
	}

	//このメソッドはこちらでいい
	private void masuIdou(ImageView v, int i, int j) {//i row, j col
		//少しずつずらしてならべる
		v.setX(kitenX-( i *hakoW/2)+( j *hakoW/2));
		v.setY(kitenY+( j *hakoW/4)+( i *hakoW/4));
	}
	private void masuIdouTx(Text v, int i, int j) {
		//少しずつずらしてならべる
		v.setX(kitenX-( i *hakoW/2)+( j *hakoW/2));
		v.setY(kitenY+( j *hakoW/4)+( i *hakoW/4));
	}

	//舞台のイメージと並び方を設定する
	private void masuNarabe() {//action

		//zeromasu();

		for(int i=0; i < flRow; i++) {
			for(int j=0; j < flCol; j++) {
				hakoView = new HakoView();
				hakoView.setId(""+0);//並び替えで使う
				hakoView.setPos(i, j);
				//hakoの半分ずれ、jが増えるごとに半分ずれる
				masuIdou(hakoView, i, j);


				//行と列の番号振りだけhakoViewには関係ない
				if(i == 0) {
					Text tx = new Text(""+ (j-1));
					tx.setId(""+0);
					masuIdouTx(tx, i, j);
					p.getChildren().add(tx);
					//field[0][j] = 0;//上端の行
					//field[ flRow-1 ][j] = 0;//下端の行
				} else if(j == 0) {
					Text tx = new Text(""+ (i));
					tx.setId(""+0);
					masuIdouTx(tx, i, j);
					p.getChildren().add(tx);
				}

				switch(field[i][j]) {
					case 0: hakoView.setImage(hako[0]); break;
					case 1: hakoView.setImage(hako[1]); break;
					case 2: hakoView.setImage(hako[2]); break;
					default: break;
				}
				p.getChildren().add(hakoView);
			}//forj,end
		}//fori,end
	}


	//パネルの属性
/*
		panel[][] == 0;					//ゼロのマス、何もできない
		panel[][] == 1;					//一のマス、何でもできる
		panel[][] == 5,10,15,20 ;		//一マス＊チーム番号＊５、キャラ（５）、チームは合流、みんな建設できない
		panel[][] == 7,14,21,28 ;		//一マス＊チーム番号＊７、空家（７）、チームは通れる、敵は通れない、みんな建設できない、みんな壊せる、壊すと１になる
		panel[][] == 9,18,27,36 ;		//一マス＊チーム番号＊９、畑（９）、チームは通れる、敵は通れない、みんな建設できない、みんな壊せる、壊すと１になる
		panel[][] == 35,70,105,140 ;	//一マス＊チーム番号＊５＊７、人家（５＊７）、チームは合流、敵は通れない、みんな建設できない、みんな変えれる、変えると家屋畑になる
		panel[][] == 45,90,135,180 ;	//一マス＊チーム番号＊５＊９、人畑（５＊７）、チームは合流、敵は通れない、みんな建設できない、みんな変えれる、変えると家屋畑になる

			team番号　＝　１～４　＊　７
		　オブジェクト番号５，７、９とチーム番号の掛け算パターンでかぶりなし
			team*7 * 5 ; team*7 * 7 ; tame*7 * 9 ; team*7 * 5 * 7 ; team*7 * 5 * 9

		panel[][] == 22;//柵、誰も通れない、みんな壊せる、壊すと１になる
		panel[][] == 23;//植物など、みんな通れない、みんな壊せる、壊すと１になる
		panel[][] == 24;//橋、みんな通れる、みんな壊せる、建設できない
		panel[][] == 25;//壊れた橋、みんな通れない、壊せない、建設できない

*/

	public boolean mustDestroy(int rowpr, int colpr, int team) {//破壊しなければならない物はあるか？
		Paneler kariPnl = panels[rowpr][colpr];
		return kariPnl.inquiry(team);//問い合わせ
	}


	public boolean panelRule2(int rowpr, int colpr, int team) {//指定の場所が建設していい所か。trueなら許可
		//同じチームの家が八方向にある場合建設できない
		boolean karibo = true;
		if( field[rowpr][colpr] >= 1 ) {//地面か？
			for(int i=-1; i < 2; i++) {
				for(int j=-1; j < 2; j++) {
					karibo = panels[rowpr+i][colpr+j].howRinka(team);//同じチームの家がない場合true
					if( karibo == false ) { break; }//falseがあれば即座にブレークし「不可」を返す
				}
				if( karibo == false ) { break; }
			}
			return karibo;
		} else {//建設使用とする場所が０マス
			return false;
		}
	}

	public void panelChange2(int rowpc, int colpc, int team, int obj, boolean inOut) {
		Paneler pnl = panels[rowpc][colpc];
		if( obj == 1 ) {//１人、２家、３畑、４もの、０なし
			if( inOut ) {
				pnl.setHitoOn(team);
			} else {
				pnl.remvHitoOn();
			}
		} else if(obj == 2) {
			if(inOut) {
				pnl.setIeOn(team);
			} else {
				pnl.remvIeOn();
			}
		} else if(obj == 3) {
			if(inOut) {
				pnl.setHataOn(team);
			} else {
				pnl.remvHataOn();
			}
		}

	}


	public boolean panelRule(int rowpr, int colpr, int team) {//指定の場所が建設していい所か
		boolean kariboo = true;

		kariboo = (panel[rowpr][colpr] == 1) ? true : false;

		for(int i=-1; i < 2; i++) {
			for(int j=-1; j < 2; j++) {
				if( panel[rowpr + i][colpr+j] == 1 + 7 * (team * 7) ||//地面＋空家
					panel[rowpr + i][colpr+j] == 1 + 5 * (team * 7) + 7 * (team * 7)//地面＋空家＋人
				) {
					kariboo = false;
					break;
				} else {
					kariboo = true;
				}
			}
			if(kariboo == false) { break; }
		}

		return kariboo;
	}

	void panelChange(int rowpc, int colpc, int team, int naru) {
		//int naruはChangeさせた後の状態
		//team * 7は補正値
		panel[rowpc][colpc] = panel[rowpc][colpc] + naru * (team * 7);

	}
	
	
	//=============================================

	public void panelPrint() {
		//fl.panelをわかりやすくプリント
		System.out.println("===== FL.PANEL =====");
		for(int i=0; i < panel.length; i++) {
			for(int j=0; j < panel[i].length; j++) {
				//桁数によるずれちょうせい
				if(panel[i][j] < 10 ) {//一桁
					System.out.print("  " + panel[i][j]);
				} else {
					System.out.print(" " + panel[i][j]);
				}
			}
			System.out.println();
		}
		System.out.println("===== FL.PANEL END =====");
	}


	private void print(Object obj, Object obj2) {
		System.out.println("  FIELD MASU  " + obj + obj2);
		System.out.println();
	}

	private void print(Object obj) {//Overrode
		System.out.println("  FIELD MASU  " + obj);
		System.out.println();
	}


	public void printArr(List arr, String str) {//汎用メソッド
		for(int i=0; i < arr.size()-1; i++) {
			System.out.println( str +" "+ i +" "+ arr.get(i) );
		}
	}
	public void printArr2(List<Node> arr, String str) {//汎用メソッド
		for(int i=0; i < arr.size()-1; i++) {
			System.out.println( str +" "+ i +" "+ arr.get(i).getId() );
		}
	}

	//=============================================
	//当クラス以外によく使用するメソッド

	public double ijouikaD(double d, double e, int kagen, int jogen) {

		if(d >= kagen) {//下限まで下げる。実際数値の上下動はここで行う -e
			d = d - e;
		} else if(d <= jogen) {//一応書いたがここまで来るのは異常
			d = d + e;
			print("IJOUIKA D");
		}

		if(d < kagen) {
			d = kagen;
		} else if(d > jogen) {
			d = jogen;
		}

		return d;
	}

	public int ijouika(int i, int j, int kagen, int jogen) {
		return (int)ijouikaD(i, j, kagen, jogen);
	}


	//==============================================	

/*
	//パネルの設定
	public void masuRect() {//Rectangleバージョン
		for(int i=0; i < field.length; i++) {
			for(int j=0; j < field[i].length; j++) {
					hako = new Rectangle( (kitenX + j*32), (kitenY + i*32), 32, 32);

					switch(field[i][j]) {
						case 0: hako.setFill(clk); break;
						case 1: hako.setFill(clg); break;
						case 2: hako.setFill(clb); break;
					}

					p.getChildren().add(hako);

			}//forj,end
		}//fori,end
	}
*/

}//class.end


//クリックイベント用にrow,colを追加
//しようとしただけだったが、panelChange,panelRuleが
class HakoView extends ImageView {
	int row;
	int col;

	public HakoView() {

	}

	public HakoView(Image img) {//Imageを指定
		setImage( img );
	}

	public HakoView(String path) {//Imageのpathだけ指定
		setImage( new Image(new File( path ).toURI().toString()) );
	}

	//---------------------------------

	protected void setPos(int i, int j) {//
		row = i;
		col = j;
	}


}//class.end


class Paneler {//パネルの属性を決める
	//Panelerを収める配列を別クラスで作る
	//なので、このクラスに配列座標の概念を設けない

	int jimen;//地面か
	boolean jimenBoo;//地面０か１か
	boolean hitoOn = false;//人がいる。このパラメータは一応残す。
	//しかし、変動の激しい人の出入りは監視対象から外す。
	//懸念される、「敵がいるのに建設する」状態を避けるため、
	//CBでコンディションというパラメータを作り、戦闘状態が優先されるようにする。
	boolean ieOn = false;//家がある
	boolean hataOn = false;//畑
	boolean monoOn = false;//その他破壊可能物がある
	int team = 0;//チーム。０は無所属。hitoOn,ieOnに準ずる


	//コンストラクタ
	public Paneler() {}
	
	public Paneler(int zeroOne) {
		this.jimen = zeroOne;
	}


	//セッター========================
	//変更が成功したかのリターンあり
	public boolean setHitoOn(int team) {//人がその場所に乗る
		return change(team, 0);
	}
	
	//セット家は敵の家が完全になくなり、team == 0になった後でなされる
	public boolean setIeOn(int team) {
		return change(team, 1);
	}

	public boolean setHataOn(int team) {
		return change(team, 2);
	}
	
	private boolean change(int team, int num) {
		if(team == this.team || this.team == 0) {//同じチームか０マス

			if( !monoOn ) {//team == 0
				this.team = team;
				
				if(num == 0) {
					hitoOn = true;
				} else if(num == 1) {
					ieOn = true;
				} else {
					hataOn = true;
				}
				
				return true;
			} else {
				
				return false;
			}
			
		} else {//違うチーム
			return false;
		}		
	}


	public boolean setMonoOn() {
		if(team == 0 && !monoOn) {
			this.team = 0;
			monoOn = true;
			return true;
		} else {
			return false;
		}
	}

	
	//リムーバー=======================
	public void remvHitoOn() {//
		if(ieOn || hataOn) {
			
		} else {
			team = 0;
		}
		hitoOn = false;
	}
	
	public void remvIeOn() {//家＋人なら、先に人がいなくなる
		team = 0;
		ieOn = false;
	}
	
	public void remvHataOn() {//家＋人なら、先に人がいなくなる
		team = 0;
		hataOn = false;
	}

	public void remvMonoOn() {
		monoOn = false;
	}
	
	
	//
	public boolean howRinka(int team) {
		//建設許可に使う。true,falseに注意。同じチームの家がない場合true
		if(ieOn) {
			return this.team != team ? true : false;
		} else {
			return true;
		}
	}
	
	
	//問い合わせinquiry
	public boolean inquiry(int youTeam) {
		//結果として、移動可能な地面であることは前提として、
		//その上に破壊しなければ行けないものがあるかどうか。
		//橋の場合、壊れた状態と壊れていない状態が逆
		
		if(team == youTeam) {
			return false;
		} else {
			if(monoOn || ieOn || hataOn || hitoOn) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean consistentCheck() {//整合性
		return false;
	}

}//class.end







