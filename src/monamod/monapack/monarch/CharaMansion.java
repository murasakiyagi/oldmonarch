package monarch;

import java.io.*;
import java.util.*;


//import javafx.animation.*;
//import javafx.application.*;
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


/*
	作意
		作業選択（目的）
			追跡（戦闘、合流）、建設、破壊、待機
			
		ターゲット（目標）選択
			敵、味方、建物、土地

		最短ルート捜査

		移動

		実働

		作業選択

	不意
		戦闘
		逃避

*/


//パネルゲーム用

public class CharaMansion {

	static Pane p;
	//static Group p;
	private int cntcm = 0;//このクラスがanimationtimerにかかる間、０～cntlimをるーぷする
	private int cntlim = 200;//cntcm上限値
	public int cntLoop = 0;
	static double lpSpeed = 1;//CM,CB,KB共通。lptim()等に掛ける倍率

	static ArrayList<CharaBuild> charalist = new ArrayList<CharaBuild>();
	static ArrayList<CharaBuild> aaTeam = new ArrayList<CharaBuild>();
	static ArrayList<CharaBuild> bbTeam = new ArrayList<CharaBuild>();
	//リストの配列
	//ArrayList<CharaBuild>[] listArry = {};

	Image img = new Image(new File( "../../img/battle.png" ).toURI().toString());

	//オリジナルクラス
	CharaBuild cbfst = new CharaBuild();//fst:ファースト
	CharaBuild cpcb;
	static Miji mi = new Miji();
	static FieldMasu fl = new FieldMasu();
	KaokuBuild kbfst = new KaokuBuild();

	//コンストラクタ
	CharaMansion() {}
	CharaMansion(/*Group p*/Pane p) {
		this.p = p;
	}



	//アクション。メインに乗せる
	void action() {
		cntcm++;
		cntLoop++;
		if(cntcm > cntlim) {
			cntcm = 0;
		}
		if(cntcm % 10 == 0) {
			//print(cntcm);
		}

	
		//if(cntLoop >= lpSpeed) {//ここでは必ずcntLoop>=1
		
		cbfst.lpSpeed = lpSpeed;
		kbfst.lpSpeed = lpSpeed;
		
			cntLoop = 0;
	
			for(CharaBuild cbA : charalist) {
				cbA.setAimAll2(charalist, kbfst.kaokulist);
				cbA.onLoop();
			}
	
			
			for(CharaBuild cbA : charalist) {
				for(CharaBuild cbB : charalist) {
					for(KaokuBuild kb : kbfst.kaokulist) {
	
						//battle(cbA, cbB);
						//hakai(cbA, kb);
						gouryuu(cbA, cbB);
	
					}
				}
			}
	
			//二重for文にcb.yarukoto()を入れると、二乗回動作するので独立させている
	/*		for(CharaBuild cbA : charalist) {
				cbA.onLoop();
			}
	*/
	
			removeCm();
			removeKb();
			kaokukun();
	
		//}//if,end(cntloop)

	}




	//セッター-------------------------------------
	void addCm(CharaBuild... cbs) {//可変長引数
		for(CharaBuild cb : cbs) {
			adddd(charalist, cb);

			if(cb.team == 1) {
				adddd(aaTeam, cb);
			} else if(cb.team == 2) {
				cpcb = cb;
				adddd(bbTeam, cb);
			}
		}

	}


	private void adddd(ArrayList<CharaBuild> arr, CharaBuild newCb) {//ダブり防止。マップとかよりListがいい
		if( !arr.contains(newCb) ) {
			arr.add(newCb);
		}
	}

	private void adddk(ArrayList<KaokuBuild> arr, KaokuBuild newKb) {//ダブり防止
		if( !arr.contains(newKb) ) {
			arr.add(newKb);
		}
	}

	//全消し
	public void zenkesi() {
		charalist.clear();
		kbfst.kaokulist.clear();
		aaTeam.clear();
		bbTeam.clear();
	}

	void removeCm() {
		for(int i=0; i < charalist.size(); i++) {
			if(charalist.get(i).hitP <= 0) {
				charalist.get(i).taijou();
				aaTeam.remove(charalist.get(i));
				bbTeam.remove(charalist.get(i));
				charalist.remove(i);
			}
		}
	}

	void removeCm(CharaBuild cb) {
		if(cb.hitP <= 0) {
			cb.taijou();
			charalist.remove(cb);
			aaTeam.remove(cb);
			bbTeam.remove(cb);
		}
	}

	void removeKb() {
		for(int i=0; i < kbfst.kaokulist.size(); i++) {
			kbfst.kaokulist.get(i).sibou();
		}
	}

	//ゲーム内の時間の速さ
	int tim = 60;
	void loopSpeed(boolean bo1, boolean bo2) {//キーイベントの結果を引数に
		if(bo1) {
			//tim = fl.ijouika(tim, 1, 30, 100);
			if(lpSpeed < 3) {
				lpSpeed++;
			}
			//cbfst.setTim(1);
			kbfst.setTax(1);//今は特別だが時間と税率は別にする
			print("LOOPSPEED", lpSpeed);
		} else if(bo2) {
			//tim = fl.ijouika(tim, -1, 30, 100);
			if(lpSpeed > 1) {
				lpSpeed--;
			}
			//cbfst.setTim(-1);
			kbfst.setTax(-1);//今は特別だが時間と税率は別にする
			print("LOOPSPEED", lpSpeed);
		} else {}
	}
	
	public void loopScoop(double d) {
		lpSpeed = d;
		cbfst.lpSpeed = d;
		kbfst.lpSpeed = d;
		//cbfst.lptimBase = (int)(cbfst.lptim() * d);
			print("LOOPSCOOP", cbfst.lptim());
		//kbfst.lptim() = kbfst.lptim() * lpSpeed;
	}

	void onajiTeam(CharaBuild cbA, CharaBuild cbB) {
		if(cbA != cbB) {

		}
	}



	//---------------------------------------------

	//ゲッター-------------------------------------
	//charalistから指定のキャラを呼び出す
	public CharaBuild getChara(int i) {
		return charalist.get(i);
	}

	public CharaBuild getChara(CharaBuild cb) {
		CharaBuild thiscb = null;
		if(charalist.contains(cb)) {
			for(int i=0; i < charalist.size(); i++) {
				if( charalist.get(i).equals(cb) ) {
					thiscb = charalist.get(i);
					break;
				} else {

				}
			}//for,end
		} else {

		}//if,end

		return thiscb;
	}


	//---------------------------------------------
	//アクション


	void battle(CharaBuild cbA, CharaBuild cbB) {
		try {
			if(cbA.team != cbB.team) {
				if(masukyoriD(cbA, cbB) <= 1.1) {

					//char hou = hougaku(cbA, cbB);
					//対戦相手に認定
					if(cbB.hitP > 0) {
						cbA.taiCb = cbB;
					} else {
						cbA.taiCb = null;
					}
					if(cbA.hitP > 0) {
						cbB.taiCb = cbA;
					} else {
						cbB.taiCb = null;
					}

					//削りあい部分
					fight(cbA, cbB);

				} else {
					//
				}


				if( (cbA.taiCb != null) && (cbB.taiCb != null) ) {//対戦相手の有無
				//if( (cbA.taiCb == cbB) || (cbB.taiCb == cbA) ) {
					//同じマスに入らぬよう
					if(cbA.hitP > cbB.hitP) {
						//cbB.atozusari();
					} else {
						//cbA.atozusari();
					}
				}

			}

		} catch(IndexOutOfBoundsException e) {
			//System.out.println("EXCEPTION " + e);
		}
	}



	void fight(CharaBuild cbA, CharaBuild cbB) {//削りあい
		double forceA = cbA.hitP / 2000;
		double forceB = cbB.hitP / 2000;

		double damageA = (cbA.hitP - forceB);//ダメージ
		double damageB = (cbB.hitP - forceA);

		cbA.setHp(damageA);
		cbB.setHp(damageB);


		//アニメ。攻撃時のviewcat
		int viewsetX = 0;

		if( cntcm < cntlim * 1/4 ) {
			viewsetX = 1;
		} else if( cntcm >= cntlim * 1/4  &&  cntcm < cntlim * 2/4) {
			viewsetX = 2;
		} else if( cntcm >= cntlim * 2/4  &&  cntcm < cntlim * 3/4) {
			viewsetX = 3;
		} else if( cntcm >= cntlim * 3/4) {
			viewsetX = 4;
		}

		cbA.viewCat(2, viewsetX);
		cbB.viewCat(2, viewsetX);
		

	}


	void hakai(CharaBuild cb, KaokuBuild kb) {//キャラが家屋を攻撃
		if(cb.team != kb.team) {
			if(masukyori(cb, kb.p2d) <= 1 &&
				cb.hitP > 0 &&
				kb.hitP > 0
			) {
				cb.taiKb = kb;
				cb.setHp( cb.hitP - 100.0 / 1000 );//kb破壊時のダメージは一定
				kb.setHp( kb.hitP - cb.hitP / 1000 );
			} else {
				cb.taiKb = null;
			}
		}
	}


	void gouryuu(CharaBuild cbA, CharaBuild cbB) {//合流
		if( !cbA.equals(cbB) &&
			(cbA.team == cbB.team) &&
			(Math.abs(cbA.rowD - cbB.rowD) < 1) &&
			(Math.abs(cbA.colD - cbB.colD) < 1)
		) {
			if( Math.max(cbA.hitP, cbB.hitP) == cbA.hitP ) {
				cbA.setHp( cbA.hitP + cbB.hitP );
				cbB.hitP = 0;
			} else {
				cbB.setHp( cbB.hitP + cbA.hitP );
				cbA.hitP = 0;
			}
		}
	}

/*
	int viecnt = 0;
	Rectangle2D rc1 = new Rectangle2D(0,0,48,48);
	Rectangle2D rc2 = new Rectangle2D(48,0,48,48);
	void viewset(ImageView view) {
		viecnt++;
		if(viecnt < 100) {
			if(viecnt % 3 == 0) {
				view.setViewport(rc1);
			} else {
				view.setViewport(rc2);
			}
		} else {
			viecnt = 0;
		}
	}
*/


	//その他---------------------------------------
	private boolean masukyoriT(CharaBuild cb1, CharaBuild cb2) {//マスとマスの距離を測る。壁は無視される
		double ro1 = cb1.rowD;		double co1 = cb1.colD;
		double ro2 = cb2.rowD;		double co2 = cb2.colD;

		if( (ro1 - ro2 == 0 && Math.abs(co1 - co2) <= 1) ||
			(co1 - co2 == 0 && Math.abs(ro1 - ro2) <= 1)
		) {
			return true;
		} else {
			return false;
		}
	}

	private double masukyoriD(CharaBuild cb1, CharaBuild cb2) {//マスとマスの距離を測る。壁は無視される
		double ro1 = cb1.rowD;		double co1 = cb1.colD;
		double ro2 = cb2.rowD;		double co2 = cb2.colD;
		return Math.abs(ro1 - ro2) + Math.abs(co1 - co2);
	}

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

	public boolean teamEnpty() {//teamsizeはゼロか？
		if(aaTeam.size() > 0 &&
			bbTeam.size() > 0 
		) {
			return false;
		} else {
			return true;
		}
	}


	private char hougaku(CharaBuild cbA, CharaBuild cbB) {//Aから見たB
		if( Math.abs(cbA.row - cbB.row) > Math.abs(cbA.col - cbB.col) ) {//同じ列で上下の関係
			if(cbA.row > cbB.row) {//自分が下
				return 'u';
			} else {
				return 'd';
			}
		} else {//同じ行で左右の関係
			if(cbA.row > cbB.row) {//自分が右
				return 'l';
			} else {
				return 'r';
			}
		}
	}

	//メイン、コンパレータで使うーーーーーーーーーーーーー
	//charalistをCharaBuildのなかの要素で探す
	public CharaBuild conta(ArrayList<CharaBuild> arr, Node nd) {
		CharaBuild kariCb = null;
		for(int i=0; i < arr.size()-1; i++) {
			if( arr.get(i).equals(nd) ) {
				//System.out.println("CMclass CONTAmeth ");
				kariCb = arr.get(i);
				break;
			}
		}
		return kariCb;
	}
	//charalistの中の要素を探す
	public Integer contaInt(ArrayList<CharaBuild> arr, Node nd) {
		Integer kari = 0;
		if(nd.getId() == null) {
			nd.setId(""+0);
		}

		for(int i=0; i < arr.size()-1; i++) {
			if( arr.get(i).equals(nd) ) {//キャラビルドか
				kari = Integer.valueOf(arr.get(i).getId()) + arr.get(i).row + arr.get(i).col;
				break;
			} else {
				kari = Integer.valueOf(nd.getId());
			}
		}
		return kari;
	}
	//ーーーーーーーーーーーーーーーーーーーーーーーーーー




	void print(Object obj, Object obj2) {
		System.out.println("  CHARA MANSION  " + obj + obj2);
		System.out.println();
	}

	void print(Object obj) {//Overrode
		System.out.println("  CHARA MANSION  " + obj);
		System.out.println();
	}



	//今のとこ使ってない---------------------------




	//---------------------------------------------
	//建物

	void kaokukun() {
		for(KaokuBuild kb : kbfst.kaokulist) {
			kb.onLoop();
		}
	}


	//===================================
	//その他

	double rnd(double d) {
		return Math.round(d*100)/100;
	}


	//===================================

}//class,end



