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

public class CharaBuild extends ImageView implements Cloneable {

	static Pane p = new Pane();//同じクラスで違うインスタンスでも同じPaneを使える
	private int cntcb = 0;//ループ、アニメタイマー用
	int cntna = 10;//nanisuru用

	//---------------------------------------------
	//ステータス
	int objNum = 1;
	String name;
	Image img;	//単体イメージ(viewportを使わない)
	ImageView hero = new ImageView();
	String vid;//ImageViewのId(set,get)
	String path;//画像のパス
	Rectangle2D[][] viewPos = new Rectangle2D[4][4];//new Rectangle2D(0,0,32,32);
	String[][] paths;
	int row, col;	//マス座標であって、xy座標ではない
	double rowD, colD;//doubleのD
	double hitP;	//体力
	Text hpTx;	//イメージに付随させる
	Text vidTx;
	int team;	//チーム。今は2チーム。heroは１、tekiは２

	//---------------------------------------------
	//状態
	boolean search;
	boolean taiki;//待機
	boolean taiBo = false;//近距離の敵がいるか
	char nanisuru = 'k';//k考え,t留まる,o追いかけ,eエスケープ,s製作,h破壊,g合流,u移動(ただの移動)
	char[] nanisurus = {'k','t','h','s','o','i'};//'e''g'なし
	char condition = 'k';//k考え,m移動,b戦い,c建造(コンストラクタ),d破壊(デストルクション)
	char preCondit = 'k';//変更前
	char[] conditions = {'k','m','b','c','d'};//これからではなく、今の状態

	//目的地
	int tgtRow, tgtCol;//目的地の軸
	Point2D tgtPd;//目的地の座標
	Point2D escPd;//退避先。一時的。tgtPdより優先
	CharaBuild tgtCb;//追いかけ、逃亡の敵対象
	CharaBuild gryCb;//合流の味方対象
	KaokuBuild tgtKb;
	ArrayList<Point2D> walkList = new ArrayList<Point2D>();//目的地までのルート
	CharaBuild taiCb;//対峙するキャラ
	KaokuBuild taiKb;//壊す家
	char houkou = 'n';//進行方向。'u','d','l','r','n'
	CharaBuild[] simensoka = new CharaBuild[4];//上０下１左２右３。十時方向に敵

	//その他
	static int lptimBase = 60;//ループタイム。大きいほど遅くなる。基本スピード６０、1.5倍速４０、2倍速３０
	static double lpSpeed = 1;//CM,CB,KB共通。lptimBaseに掛ける倍率
	static int lptim() {
		return (int)(lptimBase * lpSpeed);
	}
	//---------------------------------------------


	//オリジナルクラス
	FieldMasu fl = new FieldMasu();
	Miji mi = new Miji();
	KaokuBuild kb;
	CharaTeam ct;

	//=============================================
	//コンストラクタ
	public CharaBuild() {}

	public CharaBuild(Pane p, String name, String[][] paths, int row, int col, int hitP, int team, CharaTeam ct) {
		//this.p = p;//mainのPaneが参照される
		this.name = name;
		this.team = team;
		this.paths = paths;
// 		img = new Image(new File( paths[0][team-1] ).toURI().toString());
		img = new Image(paths[0][team-1]);
		setImage(img);
		this.path = path;
		this.row = row;
		this.col = col;
		this.rowD = row;
		this.colD = col;
		this.hitP = hitP;
		hpTx = new Text(""+(int)hitP);//「""+」でStringにキャスト
		//hpTx = new Text(""+Math.round(hitP));//「""+」でStringにキャスト
		this.team = team;
		this.ct = ct;
	}

	//=============================================
	//アクション

	void onLoop() {//ループ上で変化を監視
		//yarukoto();

		sinkohoko();
		taisenaite();
		//sibou();
		
			ct.popuStats(this, hitP);

	}
	//-----------------------------------

	//存在
	void viewCat() {//画像の切り出し
		for(int i=0; i < viewPos.length; i++) {
			for(int j=0; j < viewPos[0].length; j++) {
				viewPos[i][j] = new Rectangle2D(j*32+j, i*32+i, 32, 32);
			}
		}
	}
	
	void viewCat(int i,int j) {//画像の切り出し
		this.setViewport(viewPos[i][j]);
	}

	public void padd() {//PaneあるいはGroupに入れる
			syokiiti();
		setVid();
		setHp(hitP);
		viewCat();
		this.setViewport(viewPos[0][0]);
		p.getChildren().add(this);
		p.getChildren().add(hpTx);
		vidTx = new Text(getId());
		vidTx.setId(getId());
		masuidou(fl.hakoW, fl.hakoW/4, fl.hakoW/8);
		fl.panelChange2(row, col, team, objNum, true);
			ct.popuStats(this, hitP);
	}

	void premove() {
		p.getChildren().remove(this);//このクラスのインスタンスを消す
		p.getChildren().remove(hpTx);
		fl.panelChange2(row, col, team, objNum, false);
			ct.popuRemove(this);
	}

	void taijou() {//退場
		premove();
		//row = -1;
		//col = -1;
	}

	void sibou() {//死亡
		if(hitP <= 0) {
			taijou();
		}
	}


	void masuidou(double hakoW, double bicyouseiW, double bicyouseiH) {//hakoWは一マスの横幅
		this.setX(fl.kitenX - ( row * hakoW/2 ) + ( col * hakoW/2 ) + bicyouseiW);
		this.setY(fl.kitenY + ( col * hakoW/4 ) + ( row * hakoW/4 ) + bicyouseiH);
		hpTx.setX( this.getX() );
		hpTx.setY( this.getY() );
		//vidTx.setX( this.getX() );
		//vidTx.setY( this.getY() );
	}

	void masuidou() {//hakoWは一マスの横幅
		this.setX(fl.kitenX - ( row * fl.hakoW/2 ) + ( col * fl.hakoW/2 ) + fl.bicyouseiW);
		this.setY(fl.kitenY + ( col * fl.hakoW/4 ) + ( row * fl.hakoW/4 ) + fl.bicyouseiH);
		hpTx.setX( this.getX() );
		hpTx.setY( this.getY() );
		//vidTx.setX( this.getX() );
		//vidTx.setY( this.getY() );
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

	//セッター-------------------------------------
	void setPosition(int row, int col) {
		this.row = row;
		this.col = col;
		masuidou(fl.hakoW, fl.hakoW/4, fl.hakoW/8);
	}

	void setHp(double h) {
		hitP = h;
		hpTx.setText(""+(int)hitP);
	}

	void gouryu(CharaBuild cb) {//合流。ユニット同士の合体
		hitP = hitP + cb.hitP;
		hpTx.setText(""+hitP);
	}


	void setImg(String newPath) {//画像の変更
		img = new Image(new File( newPath ).toURI().toString());
		this.setImage( img );
	}

	void setWalkList(ArrayList<Point2D> arr) {
		walkList = arr;
	}

	void setTarget(int tgtRow, int tgtCol) {
		this.tgtRow = tgtRow;
		this.tgtCol = tgtCol;
		tgtPd = new Point2D(tgtRow, tgtCol);
	}

	void setKaoku(KaokuBuild kb) {
		this.kb = kb;
	}

	public void setTim(int i) {//デバッグ用。移動速度とか
		lptimBase = fl.ijouika(lptimBase, i * 3, 30, 100);
	}

	void changeImg() {
		/*パラメータの状態によってイメージを変える
		if(taiCb != null) {
			setImg();
		}
		*/
	}

	void setVid() {//paddより前にセット
		fl.vidcnt++;
		this.setId(""+ (fl.vidcnt + 1000));
		hpTx.setId(""+ (fl.vidcnt + 1000));
	}


	public void conditioner(char cha) {
		preCondit = condition;
		condition = cha;
	}
	
	public void reConditioner() {
		condition = preCondit;
	}



	//行動規範=======================================
/*	
	１、家を建てる's'
	２、敵の家を壊す'h'
	３、敵を倒す（追いかける）'o'
	これらを行う際に、邪魔な敵や破壊可能な物を破壊する

	他者の状況に左右されることばかりなのでシングルスレッド

*/
	public void koudoukihan() {
		boolean kimatta = false;//何するか決まった

		if(nanisuru == 'k') {

			nanisuru = 's';//最優先

			//自分の周りに建設可能か
			for(int i=-1; i < 2; i++) {
				for(int j=-1; j < 2; j++) {
					if(fl.panelRule2(row+i, col+j, team)) {
						tgtPd = new Point2D(row+i, col+j);
						kimatta = true;
					}
				}
			}
			
			if( !kimatta ) {
				
			}
		
		}//end,if = 'k'
		
		
	}



	//tgtCb, tgtPd, gryCb。inloop
	int kyoriOld;//設定されたtgtCbとの距離
	int kyoriNew;//新newTgtとの距離
	public void setAimAll2(ArrayList<CharaBuild> arrcb, ArrayList<KaokuBuild> arrkb) {//CMにて二重for内

		if( !arrcb.contains(tgtCb) ) {//設定されていたtgtCbはリストにあるか
			tgtCb = null;
		}
		if( !arrcb.contains(gryCb) ) {
			gryCb = null;
		}
		if( !arrcb.contains(taiCb) ) {
			taiCb = null;
		}
		if( !arrkb.contains(tgtKb) ) {
			tgtKb = null;
		}
		if( !arrkb.contains(taiKb) ) {
			taiKb = null;
		}


		//ここで得られるnewTgtはただの情報である（可能性がある）。
		for(CharaBuild newTgt : arrcb) {
			
			
			kyoriNew = masukyori(this, newTgt);
			if(newTgt.team != team) {//別チーム======================
	
				if(hitP >= newTgt.hitP) {//newTgtは自分より弱い
	
					if(tgtCb == null) {
						tgtCb = newTgt;
						kyoriOld = masukyori(this, tgtCb);
					} else {
						kyoriOld = masukyori(this, tgtCb);
					}
					//newtgtの方が近ければ、tgtCb書き換え
					if(kyoriOld >= kyoriNew) {
						tgtCb = newTgt;
					} else {}
	
				} else {//newTgtは強い
					//自分より強い敵から一歩退く
					if(escPd == null) {
	
						if(kyoriNew < 3) {//強敵に近い
							for(int i = -1; i <= 1; i++) {
								for(int j = -1; j <= 1; j++) {
									//現在地から八方向に動くとして
									kyoriNew = masukyori( (row + i), (col + j), newTgt.row, newTgt.col );
									//退く先が距離X以上なら
									if( kyoriNew >= 3 &&
										fl.field[row + i][col + j] != 0
									) {
										nanisuru = 'e';
										escPd = new Point2D(row + i, col + j);
									} else {
										nanisuru = 'k';
										escPd = null;
									}//if,end()
									
								}//for,end
							}//for,end
						}//if,end(kyoriNew)
	
					} else {
	
					}//if,end(escPd)
	
				}//if,end(hitP)
				
				
				//battle
				if(kyoriNew <= 1.1) {
					taiCb = newTgt;
				} else {
					
				}
				
	
			} else {//同じteam。合流する相手==============
	
				if(newTgt.hitP > hitP) {//相手が強かったら、合流したくなる
				
					//nanisuru = 'o';//'o'は今の所gryCbに対応していない
				
					if(gryCb == null) {
						gryCb = newTgt;
						kyoriOld = masukyori(this, gryCb);
					} else {
						kyoriOld = masukyori(this, gryCb);//古い合流相手
						if(kyoriOld >= kyoriNew) {
							gryCb = newTgt;
						} else {}
					}
					
				}
	
			}//if,end team========================================
			
			
		}//end,for(arrcb)


		while(tgtPd == null) {//tgtPdがあるなら素通り
			//適当な場所
			int ro = (int)(Math.random() * fl.flRow);
			int co = (int)(Math.random() * fl.flCol);
			Point2D kariPd = new Point2D(ro,co);

			if( fl.panel[ro][co] == 1 &&
				//fl.panel[ro][co] != team &&
				masukyori(this, kariPd) < 6 &&
 				masukyori(this, kariPd) != 0
			) {
				tgtPd = kariPd;
			} else {}
		}//while


		for(KaokuBuild newKao : arrkb) {
			
			kyoriNew = masukyori(this, newKao.p2d);
			
			if(newKao.team != team) {//別チームの家屋、破壊対象
				//nanisuru='h'は別の処理で決定する
				if(tgtKb == null) {
					tgtKb = newKao;
				} else if( masukyori(this, tgtKb.p2d) > masukyori(this, newKao.p2d) ) {
					tgtKb = newKao;
				} else {}


				//hakai
				if(kyoriNew <= 0) {
					taiKb = newKao;
				} else {
					
				}


			}
			
			
		}


		//今までCM.action()に入れていたがここに入れてみる
		yarukoto2();

	}//setAimAll2.end
	
	
	
	private void yarukoto2() {

		try{

			if(taiCb != null) {
				//setHp(hitP - taiCb.hitP/1000);
			} else if(taiKb != null) {
				//setHp(hitP - 0.1);
				//print("  TGT KB  ", tgtKb);
			//やることを決める
			} else if(nanisuru == 'k') {
			//if(nanisuru == 'k') {
				cntcb++;
				if(cntcb < 10) {
					viewCat(0,0);
				} else {

					if(hitP <= 50) {
						nanisuru = 'g';
					} else {
						//現在地の十字に建設ができるか
						for(int i=-1; i < 2; i++) {
							for(int j=-1; j < 2; j++) {
	
								if(fl.panelRule2(row + i, col + j, team) && ct.money >= 50) {
									//fl.mustDestroy(row + i, col + j, team);//現場到着時にこのメソッドを使うのでここには必要ない
									nanisuru = 's';
									tgtPd = new Point2D(row+i, col+j);
									break;
								} else {
									//nanisurusサイズは６で、最後はuただ動くだけなのでこれを回避、uの前はoなのでtgtCbがないならしない
									nanisuru = nanisurus[(int)( Math.random() * (tgtCb != null ? 5 : 4) )];
								}
	
							}
							if(nanisuru == 's') { break; }
						}
						
					}

					cntcb = 0;
				}//if,end

			//一定時間その場にとどまる
			} else if(nanisuru == 't') {
				cntcb++;
				if(cntcb < 90) {
					viewCat(0,0);
					//
				} else {
					nanisuru = 'k';
					cntcb = 0;
				}

			//ただの移動
			} else if(nanisuru == 'i') {
				viewCat(0,1);
				if(walkList.size() == 0) {
					setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );
				} else {
					susumukunD(true);
				}
				
				//到着したら
				if(walkList.size() == 0) {//susumukunDにてwalkList.clear()になる
					nanisuru = 'k';
				}


			//追っかけ
			} else if(nanisuru == 'o' && tgtCb != null) {
					viewCat(0,1);

				if(walkList.size() == 0) {
					tgtPd = new Point2D(tgtCb.row, tgtCb.col);//susumukunの終了条件がtgtPdなので
					setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );

				} else {
					susumukunD(true);
				}


				if(tgtCb != null) {
					if( masukyori(this, tgtCb) <= 1 ) {//例外出やすい！！tgtCb=nullが原因。上記のif文でエラーは出ていない
						walkList.clear();
						nanisuru = 'k';
					}
				} else {
					
				}

			} else if(nanisuru == 'g' && gryCb != null) {
					viewCat(0,1);

				if(walkList.size() == 0) {
					tgtPd = new Point2D(gryCb.row, gryCb.col);//susumukunの終了条件がtgtPdなので
					setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );

				} else {
					susumukunD(true);
				}


					if(gryCb != null) {
						if( masukyori(this, gryCb) <= 1 ) {//例外出やすい！！tgtCb=nullが原因。上記のif文でエラーは出ていない
							walkList.clear();
							nanisuru = 'k';
						}
					}

			//逃げる
			} else if(nanisuru == 'e') {
					viewCat(0,2);
				if(escPd != null) {
					if(walkList.size() == 0) {
						tgtPd = escPd;
						setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );
					} else {
						susumukunD(true);

					}


					if( escPd.equals(new Point2D(row, col)) ) {
						print("エスケープ");
						escPd = null;
						walkList.clear();
						nanisuru = 'k';
					}


				} else {
					nanisuru = 'k';
				}//ifend

			//破壊
			} else if(nanisuru == 'h') {
					viewCat(0,3);
				if(walkList.size() == 0) {
					tgtPd = new Point2D(tgtKb.row, tgtKb.col);//susumukunの終了条件がtgtPdなので
					setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );
				} else {
					susumukunD(true);
				}

				if(masukyori(this, tgtKb.p2d) <= 1) {
					walkList.clear();
					if(tgtKb.hitP > 0) {
						//破壊処理
					} else {
						tgtKb = null;
						nanisuru = 'k';
					}
				} else {}

			//製作
			} else if(nanisuru == 's') {
					viewCat(0,3);
				if(walkList.size() == 0) {
					setWalkList( mi.okkake(row, col, (int)tgtPd.getX(), (int)tgtPd.getY()) );
				} else {
					susumukunD(true);
				}

				if(masukyori(this, tgtPd) <= 1) {
					walkList.clear();
					cntcb++;
					//現場まで行った時点でもう一度建設許可の確認
					if( fl.panelRule2( (int)tgtPd.getX(), (int)tgtPd.getY(), team) && ct.money >= 50 ) {

						if( cntcb >= 10 ) {//到着後すぐに作らない演出
							KaokuBuild newkaoku = new KaokuBuild(p, name, paths, (int)tgtPd.getX(), (int)tgtPd.getY(), 50, team, this);
							newkaoku.padd();
							setHp(hitP - 50);
							cntcb = 0;
							tgtPd = null;
							nanisuru = 'k';
								ct.billExpense();
								print("  =====人工と税金  ", team);
								print(ct.money, ct.moneyD);
								print(ct.popula, ct.popuMap.size());
								print("=====");
						}

					} else {//やっぱり作れなかった
						tgtPd = null;
						nanisuru = 'k';
						cntcb = 0;
							if(ct.money < 50) {
								print("  MONEY  IS  ENPTY !!");
							}
						
					}
				} else {
					//nanisuru = 'k';
				}//if,end(masukyori)
			}//if,end('s')

		} catch(Exception e) {
			nanisuru = 'k';
						System.out.println();
						System.out.println("-----CAHRAclass  NANISURU s Not " + tgtPd +" "+ team +" "+ kb.kinsikuiki.size());
						System.out.println();
			e.printStackTrace();
		}

	}//yarukoto2,end




	//ゲッター-------------------------------------
	public void printStatus() {
		System.out.println("CHARABUILD " + name +" "+ path +" "+ row  +" "+ col +" "+ hitP +" "+ team);
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public double getHp() {
		return hitP;
	}

	public Point2D getTgtpd() {
		return tgtPd;
	}

	//---------------------------------------------
	//ギフター、渡す
	private void gifFtpr(int i) {
		if(walkList.size() > 0) {
			row = (int)walkList.get(i).getX();
			col = (int)walkList.get(i).getY();
			fl.panelChange2(row, col, team, objNum, true);
		} else {
			//
		}
	}


	@Override//Objectクラスのやつ。protected固定
	protected CharaBuild clone() {
		CharaBuild clone;
		try {
			clone = (CharaBuild)super.clone();
		} catch(Exception e) {
			clone = null;
				System.out.println("CHARA class  CLONE meth  catch");
		}
		return clone;
	}


	public CharaBuild copy(int r, int c) { //throws Exception使用する場合引数かっこの後に置き、実装側でtry-catchする
		CharaBuild cpy = new CharaBuild(p, name, paths, r, c, 100, team, ct);
		return cpy;
	}



	//---------------------------------------------





	//.............................................

	private void sinkohoko() {//inLoop
		//微調整。
		if(walkList.size() > 0) {

		}

		int sig = (int)Math.signum(row - rowD);
		if(sig == 1) {//'u'
			houkou = 'u';
		} else if(sig == -1) {
			houkou = 'd';
		} else {
			//
		}

		sig = (int)Math.signum(col - colD);
		if(sig == 1) {//'l'
			houkou = 'l';
		} else if(sig == -1) {
			houkou = 'r';
		} else {
			//
		}

	}

	private void taisenaite() {//inLoop
		if(taiCb != null && taiCb.hitP <= 0) {
			taiCb = null;
		}
		if(taiKb != null && taiKb.hitP <= 0) {
			taiKb = null;
		}
	}



	//=============================================
	//doubleバージョン。loopに直置き
	private void masuidouD() {//hakoWは一マスの横幅
		this.setX(fl.kitenX - ( rowD * fl.hakoW/2 ) + ( colD * fl.hakoW/2 ) + fl.bicyouseiW);
		this.setY(fl.kitenY + ( colD * fl.hakoW/4 ) + ( rowD * fl.hakoW/4 ) + fl.bicyouseiH);
		hpTx.setX( this.getX() );
		hpTx.setY( this.getY() );
		//vidTx.setX( this.getX() );
		//vidTx.setY( this.getY() );
	}

	//int cntft = 0;
	private void susumukunD(boolean go) {//ftprに沿って移動
		//row,colは現在地 ftpr(0)は次のマス
		if(go) {

			if(taiCb == null && taiKb == null) {
				if(rowD == 0) {//初期化。初期化後の値が０になる事はないので
					rowD = row;
					colD = col;
				}

				if(tgtPd.equals(new Point2D(row,col))) {//tgtPdに到着
					walkList.clear();

				} else if(walkList.size() > 0) {//進行中
					//lptimは1マスの距離を分割する数
					cntcb++;//lptim = 60、なら１ループで1/60マスすすむ

					if(cntcb < lptim()) {

						if(row - walkList.get(0).getX() == 0.0) {//次のマスがcol方向
							rowD = row;//微調整
						} else if(row > walkList.get(0).getX()) {//次のマスが今より上方向なら
							//現在地(row) + ( 次のマス(ftpl) - (1～59) )
							rowD = row + (walkList.get(0).getX() - cntcb) / lptim() - row / lptim();
						} else if(row < walkList.get(0).getX()) {
							rowD = row + (walkList.get(0).getX() + cntcb) / lptim() - row / lptim();
						}
						
						if(row - walkList.get(0).getY() == 0.0) {
							colD = col;
						} else if(col > walkList.get(0).getY()) {
							colD = col + (walkList.get(0).getY() - cntcb) / lptim() - row / lptim();
						} else if(col < walkList.get(0).getY()) {
							colD = col + (walkList.get(0).getY() + cntcb) / lptim() - row / lptim();
						} 

							/*　式の説明
								rowD : 現在地
								row : 今のマスの中心。現在地の始点
								ftpr.get(0) : 次のマスの中心
								cntcb : ループ回数
								lptim : 中心間の分割数。
								中心から中心まで(cntcb / lptim)ループかかる

									rowD = row + (walkList.get(0).getX() - cntcb) / lptim - row / lptim;

									始点 ＋ （到着点、ー、ループ回数）/ 分割数 ー 始点 / 分割数
								
								「カッコで囲われた二項 / 分割数」引く 「始点 / 分割数」の部分は分母が同じ分割数になっているのが肝
								到着点　ー　始点　ー　分割数でもいい

							*/

						masuidouD();

					} else if(cntcb >= lptim()) {
						//移動元パネルを戻す
						fl.panelChange2(row, col, team, objNum, false);
						gifFtpr(0);//walkListのXYをrow,colに渡す。パネル変更内包
						//この時点で「walkList(0) == 現在地」である
						walkList.remove(0);
						//この時点で「walkList(0) == 次のマス」である
						cntcb = 0;

					}

				} else {
					search = true;
				}

			} else {//taiCb != null

			}

		} else {//not go

		}
	}


	public void atozusari() {//後ずさり。敵と同じマスに入りそうになったら使う
			if(walkList.size() > 0) {
			}
			//walkList.clear();//susumukunを止める


			row = (int)Math.round(rowD);
			col = (int)Math.round(colD);

			masuidou();//Dじゃない
			//nanisuru = 'k';

					viewCat(1,0);

	}
	//=============================================
	private int masukyori(CharaBuild cb1, CharaBuild cb2) {//マスとマスの距離を測る。壁は無視される
		int ro1 = (int)cb1.getRow();		int co1 = (int)cb1.getCol();
		int ro2 = (int)cb2.getRow();		int co2 = (int)cb2.getCol();
		return Math.abs(ro1 - ro2) + Math.abs(co1 - co2);
	}
	private int masukyori(CharaBuild cb1, Point2D cb2) {//マスとマスの距離を測る。壁は無視される
		int ro1 = (int)cb1.getRow();		int co1 = (int)cb1.getCol();
		int ro2 = (int)cb2.getX();		int co2 = (int)cb2.getY();
		return Math.abs(ro1 - ro2) + Math.abs(co1 - co2);
	}
	private int masukyori(int ro1, int co1, int ro2, int co2) {//マスとマスの距離を測る。壁は無視される
		return Math.abs(ro1 - ro2) + Math.abs(co1 - co2);
	}

	void susumukun() {//ftprに沿って移動
		if(tgtPd.equals(new Point2D(row,col))) {
			walkList.clear();
			search = true;
		} else if(walkList.size() > 0) {
			row = (int)walkList.get(0).getX();
			col = (int)walkList.get(0).getY();
			masuidouD();
			walkList.remove(0);
		} else {
			search = true;
		}
	}



	void print(Object obj, Object obj2) {
		System.out.println("  CHARA BUILD  " + obj +"  "+ obj2);
		System.out.println();
	}

	void print(Object obj) {//Overrode
		System.out.println("  CHARA BUILD  " + obj);
		System.out.println();
	}



}//class,end



