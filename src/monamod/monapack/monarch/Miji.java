//自動追尾、最短距離アルゴリズムほぼ完成品
//一部不具合があるが、FieldMasuの生成やCharaBuildのtgtCbなどが原因かと

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
import javafx.concurrent.*;
import javafx.event.*;

//斜めマス配置。ｉとｊを入れ替えで逆に。
//rectの色柄で配置の確認が取れる
//Mijikai6からippo部分を変更

public class Miji {

	int i;

	Point2D startPd;//移動する者の初期位置
	Point2D targetPd;//目的地
	int tgtRow, tgtCol;	//相手のマス座標、縦、横
	int slfRow, slfCol;	//自分
	int gstRow, gstCol;//slfRowの幽体。ghostごーすと
	int ripRow, ripCol;//gstRowの波紋。rippleりぷる

	int cntM = 0;
	boolean search = true;//捜査するか？

	Map<Point2D, Integer> ftcntMap = new HashMap<>();//座標と歩数を紐づけ

 	ArrayList<Point2D> ftprList = new ArrayList<Point2D>();//踏んだところ。一時的に壁になる
	ArrayList<Point2D> stopList = new ArrayList<Point2D>();//立ち止まって（次に進めなくて）stopcnt >= 4になった所。
 	ArrayList<Point2D> tranList = new ArrayList<Point2D>();//通過点（曲がり角）
 	ArrayList<Point2D> walkList = new ArrayList<Point2D>();//歩道

	//行ける十字線
	ArrayList<Point2D> crosListG = new ArrayList<Point2D>();//ゴール地点から十字線
	ArrayList<Point2D> crosListS = new ArrayList<Point2D>();//スタート地点から
	ArrayList<Point2D> crosListT = new ArrayList<Point2D>();//transitから
	ArrayList<Point2D> crosListE = new ArrayList<Point2D>();//探索中のマスから,explore探検

	Point2D[] parr = new Point2D[2];//敵が動いたら？

	int ftcnt1 = 0;//実体の歩数。ftcntMaping()により取得
	int ftcnt2 = 0;//十字線上の歩数。波紋ripの距離。masukyori()
	int ftcnt3 = 0;//歩数保持１＋２
	int stopcnt;//一歩進めなかったカウント


	//オリジナルクラスFieldMasuから引き継ぐ値
	static ArrayList<Point2D> zeroList;


	//コンストラクタ=========================================
	public Miji() {
		zeroList = FieldMasu.zeroList;
	}

	public Miji(CharaBuild cb, int targetRow, int targetCol) {
		zeroList = FieldMasu.zeroList;
		slfRow = cb.row;
		slfCol = cb.col;
		tgtRow = targetRow;
		tgtCol = targetCol;
	}
	//=======================================================

	void print() {
		//System.out.println("MIJI PRINT " + hakoW);
	}
	void debug(String s) {
		System.out.println("  MIJI  DEBUG  " + s);
	}



	//略語メソッド
	private Point2D P2D() {
		return new Point2D(gstRow,gstCol);
	}
	private Point2D P2D(int ro, int co) {
		return new Point2D(ro,co);
	}


	private void printArray(List arr, String str) {//汎用メソッド
		for(int i=0; i < arr.size()-1; i++) {
			System.out.println( str +" "+ arr.get(i) );
		}
	}


//-----------------------------------------------------------
//各オブジェのスタート地点から、直線状のマスリスト化

	private void bodysoul() {//離れた精神と体を重ねる
		gstRow = slfRow;
		gstCol = slfCol;
	}

	private void furidasi() {//歩数を保持しつつ、振出しに戻る。利用数３
		ftcnt3 = ftcnt1 + ftcnt2;
		ftcnt1 = 0;
		ftcnt2 = 0;
	}

	private void addAbsent(ArrayList<Point2D> arr, int ro, int co) {//唯一のしか入れない
		if( !arr.contains(P2D(ro,co)) ) {
			arr.add(P2D(ro,co));
		}
	}

	private void ftcntMapPutAbsent(int cnt) {//唯一のしか入れない
		if( !ftcntMap.containsKey( P2D() ) ) {//本当はこのif条件はいらない
			ftcntMap.putIfAbsent( P2D(), cnt );//putIfAbsent()はkeyと値を持っていない場合に追加する
		} else if( ftcntMap.get( P2D() ) >= cnt ) {//キー(P2D)のバリューより今回の数値以下なら。以下にするのは可能性を広げるため
			ftcntMap.put( P2D(), cnt );
		}
	}

	//壁にぶつかるまで進む十字線。超重要
	private void zeroCrosAdd(ArrayList<Point2D> arr, Point2D p, int ro, int co) {
		arr.clear();

		for(int i=-1; i <= 1; i=i+2) {//下左、上右の二回分
			while( !zeroList.contains(P2D(ro,co)) ) {//壁にぶつかるまでループ
				addAbsent(arr,ro,co);
				ro = ro + i;
			}
			//一旦元の値に戻す
			ro = (int)p.getX();
			co = (int)p.getY();

			while( !zeroList.contains(P2D(ro,co)) ) {
				addAbsent(arr,ro,co);
				co = co + i;
			}
			ro = (int)p.getX();
			co = (int)p.getY();
		}
	}

	//Point2D p の上下左右の４点をftcntMapに追加
	//このメソッドが起こる地点は立ち入り可能が保証されている
	private void ftcntMaping(int cnt, Point2D p) {//実行する場合(ftcnt1, P2D())
		//当メソッド内でP2Dの内容は変化するが、引数として受け取ったpは不変であるので、この引数が必要。
		int mk;

		for(int i=-1; i <= 1; i=i+2) {
			//P2D()はwhile内で移動する。０マスに当たるまで
			while( !zeroList.contains(P2D()) ) {
				mk = masukyori(p, P2D());	//任意の座標と幽体の距離
				ftcntMapPutAbsent(cnt + mk);	//ftcntMapに幽体の座標と歩数をプット
				gstRow = gstRow + i;
			}
			//起点に戻る
			gstRow = (int)p.getX();
			gstCol = (int)p.getY();

			while( !zeroList.contains(P2D()) ) {
				mk = masukyori(p, P2D());
				ftcntMapPutAbsent(cnt + mk);
				gstCol = gstCol + i;
			}
			gstRow = (int)p.getX();
			gstCol = (int)p.getY();
		}
	}


//----------------------------------------------------------
//ランダムに動いて目標を見つける

	private void ippo(int i, int j) {//現在地の隣に行けたら行く。okkake()
		//i,j = -1～1

		if(
			!(zeroList.contains(P2D(gstRow + i, gstCol + j))) &&
			!(ftprList.contains(P2D(gstRow + i, gstCol + j))) &&
			!(stopList.contains(P2D(gstRow + i, gstCol + j)))
		) {
			gstRow = gstRow + i;
			gstCol = gstCol + j;
			ftprList.add(P2D());
			ftcnt1 = ftcntMap.get(P2D());//その座標の歩数
			ftcntMaping(ftcnt1, P2D());//Mapにデータを格納
			zeroCrosAdd(crosListE, P2D(), gstRow, gstCol);//crosListE
			stopcnt = 0;
				//System.out.println("IPPO "+ gstRow +" "+ gstCol);

		} else { stopcnt++; }
	}

/*
	private void masuhoui(Point2D slfPd, Point2D tgtPd) {//ippo()。方位
		int ro1 = (int)slfPd.getX();		int co1 = (int)slfPd.getY();
		int ro2 = (int)tgtPd.getX();		int co2 = (int)tgtPd.getY();
		int ro3;
		int co3;

		ro3 = ro2 - ro1;
		co3 = co2 - co1;

		if( (Math.signum(ro3) == 1) && (Math.signum(co3) == 1) ) {
			
		}
	}
*/



	private int masukyori(Point2D slfPd, Point2D tgtPd) {//マスとマスの距離を測る。壁は無視される
		int ro1 = (int)slfPd.getX();		int co1 = (int)slfPd.getY();
		int ro2 = (int)tgtPd.getX();		int co2 = (int)tgtPd.getY();

		return Math.abs(ro1 - ro2) + Math.abs(co1 - co2);
	}


	private void oneWalk(Point2D slfPd, Point2D tgtPd) {//slfPdマスをtgtPdマスに一歩ずつ近づける。walkList
		int ro1 = (int)slfPd.getX();		int co1 = (int)slfPd.getY();
		int ro2 = (int)tgtPd.getX();		int co2 = (int)tgtPd.getY();
		int ro3;
		int co3;

		ro3 = ro2 - ro1;
		co3 = co2 - co1;

		//Math.signum(double):±の符号を得る。1,0,-1。
		if(ro3 != 0) {
			for(int i=0; i < Math.abs(ro3); i++) {//RO3
				slfPd = slfPd.add( (int)Math.signum(ro3), (int)Math.signum(co3) );//Point2DのXYの値に足し算
				addAbsent(walkList, (int)slfPd.getX(), (int)slfPd.getY() );
			}
		} else {
			for(int i=0; i < Math.abs(co3); i++) {//CO3
				slfPd = slfPd.add( (int)Math.signum(ro3), (int)Math.signum(co3) );
				addAbsent(walkList, (int)slfPd.getX(), (int)slfPd.getY() );
			}
		}
	}



//----------------------------------------------------------

	//追いかける
	public ArrayList<Point2D> okkake(int slfRow, int slfCol, int tgtRow, int tgtCol) {

		//自分の場所から敵などの場所
		this.slfRow = slfRow;
		this.slfCol = slfCol;
		this.tgtRow = tgtRow;
		this.tgtCol = tgtCol;

		cntM++;
		bodysoul();//初期化


		//敵が動いたら
		if(parr[0] == null) {
			parr[0] = P2D(tgtRow,tgtCol);
		} else if( !parr[0].equals(P2D(tgtRow,tgtCol)) ) {
			ftcntMap.clear();
		}


		zeroCrosAdd(crosListG, P2D(tgtRow,tgtCol), tgtRow, tgtCol);//ゴールから十字


//		if(cntM >= 0) {//必要ないかも

			ftcnt1 = 0;//初期化
			ftcntMaping(ftcnt1, P2D());//幽体十字線をftcntMapに格納。スタート地点
			zeroCrosAdd(crosListS, P2D(slfRow,slfCol), gstRow, gstCol);//スタート地点の十字線


			boolean judge = false;
			int brkcnt = 0;//無限ループ防止
			while( brkcnt < 10000 ) {

				brkcnt++;

					//ブレーク
				//transitと敵初期位置の交差
				for(Point2D p : crosListS) {
					//「スタート地点から”見える”所」に、「ゴールから見える所」もしくは「通過点から見える所」がある
					if( ( crosListG.contains(p) || crosListT.contains(p) ) &&
						ftcnt3 >= ftcnt1 + ftcnt2
					) {
						addAbsent(tranList, (int)p.getX(), (int)p.getY());

						crosListT.clear();//必要。SとGはここに来るまでに書き換えがあるがTはない。
						bodysoul();
						furidasi();

						judge = true;
						break;//for
					} else {
						judge = false;
					}//if,end

				}//for,end

				if(judge) {
					break;//while
				}

//judge == falseなら以下の処理をする
//----------------------------------------------------------
//幽体を進める
				int ransu = (int)(Math.random()*4);

				//ippo幽体が一歩進み、ftprList,ftcntMap,crosListEを書き換える
				switch(ransu) {
					case 0:
						ippo(1,0);//下
						break;
					case 1:
						ippo(0,1);//右
						break;
					case 2:
						ippo(-1,0);//上
						break;
					case 3:
						ippo(0,-1);//左
						break;
					default:
						break;
				}


//----------------------------------------------------------
//幽体が一歩進んだ後

				for(Point2D p : crosListE) {//探査中のマスから見える所

					ripRow = (int)p.getX();
					ripCol = (int)p.getY();

					//ftcnt2 = masukyori(new Point2D(ripRow, ripCol), p);//必要か？ ftcnt2の数値の代入はここだけである

					//初めて見つけた。通過点が今までない
						//補足。幽体がスタート地点から十字線を出しながら一歩ずつ進む。
						//現在地の幽体が出す十字線crosListEに、ゴールの十字線が掛かるとその交点がtransit、つまりtranListに追加される
						//スタート地点に戻り、一歩ずつ進みtransitからの十字線を探し、crosListTとcrosListSが交われば終了する。
					if( crosListG.contains( p ) && 
						( tranList.size() == 0 || ftcnt3 >= ftcnt1 + ftcnt2 )
					) {
						tranList.clear();//tranList初登場なので初期化
						addAbsent(tranList, ripRow, ripCol);//
						zeroCrosAdd(crosListT, p, ripRow, ripCol );
						bodysoul();//スタート地点に戻る
						furidasi();
						break;//for
					}

					//transit十字線を見つけた
					if( crosListT.contains( p ) &&
						ftcnt3 >= ftcnt1 + ftcnt2
					) {
						addAbsent(tranList, ripRow, ripCol);
						zeroCrosAdd(crosListT, p, ripRow, ripCol );
						bodysoul();
						furidasi();
						break;//for
					}

				}//foreach,end



				if(stopcnt >= 4) {//四方のいずれも行けない場合、その地点が壁になる
					ftprList.clear();
					stopList.add(P2D());
					stopcnt = 0;
				}

			}//while,turn



			if(brkcnt >= 10000) {//無限ループ防止
				search = true;
			} else {
				search = false;
			}


			//目標、通過点、始点をつなぐ-----------------------
			tranList.add(0, P2D(tgtRow, tgtCol));//リストの最初に入れる
			tranList.add(P2D(slfRow, slfCol));//リストの最後
			for(int i=tranList.size()-1; i >= 1; i--) {
				oneWalk(tranList.get(i), tranList.get(i-1));
			}
			//-------------------------------------------------

			//最終チェック-------------------------------------
			for(int i=0; i < walkList.size()-1; i++) {
				//次のマスは隣か、０マスに乗らないか
				
				//KYORIエラーが出る場合はtgtPdが途中で死亡した場合と考えられる
				if( masukyori(walkList.get(i), walkList.get(i+1)) != 1 ) {
					print("=========LASTCHECK  KYORI================");
						for(Point2D pd : walkList) {
							print(pd);
						}
					search = true;
					break;
				}

				//ZEROエラーが出るのは、多くはキャラが増えすぎてからなので、処理落ちかと思う
				if(zeroList.contains(walkList.get(i))) {
					print("=========LASTCHECK  ZERO================");
						for(Point2D pd : walkList) {
							print(pd);
						}
					search = true;
					break;
				}
			}
			//-------------------------------------------------

			if( search ) {//無限ループ防止で引っかかった
					debug("clear");
				walkList.clear();
			}


			ftprList.clear();
			stopList.clear();
			bodysoul();

			tranList.clear();

			ftcntMap.clear();

			cntM = 0;

//		}//if40,end

		return walkList;

	}//okkake,end


	void print(Object obj, Object obj2) {
		System.out.println("  MIJI  " + obj + obj2);
		System.out.println();
	}

	void print(Object obj) {//Overrode
		System.out.println("  MIJI  " + obj);
		System.out.println();
	}

}
