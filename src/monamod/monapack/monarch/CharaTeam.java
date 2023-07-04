//今の所、moneyだけがこのクラスの存在意義で、ほぼほぼKaokuBuildでカバーできるが、後々何かあるかもしれないので作って使うことにする。

package monarch;

import java.io.*;
import java.util.*;


public class CharaTeam {

	//メインでチームごとにstaticインスタンス化。そしてそれをCharaBuildに追加する。

	//メンバーはstaticじゃない
	int cntct = 0;
	int team;
	int money;
	double moneyD = 0.0;
	int taxrate = 10;
	int popula;//人口
	int lptimBase = 60;
	double lpSpeed = 1;
	int lptim() {
		return (int)(lptimBase * lpSpeed);
	}
	
	HashMap<CharaBuild,Integer> popuMap = new HashMap<CharaBuild,Integer>();

	//勘違い。このクラスはCharaBuildが所持する属性のほんの一部（塊）で、このクラスがCharaを持つというのは間違った認識。
	//つまり、チームが誰を持っているか、ではなく、誰がどのチーム（属性）を持っているか、なのだ。
	//ArrayList<CharaBuild> charaList = new ArrayList<CharaBuild>();//staticじゃない
	//ArrayList<KaokuBuild> kaokuList = new ArrayList<KaokuBuild>();
	

	//コンストラクタ
	public CharaTeam() {}


	public CharaTeam(int team, int money) {
		this.team = team;
		this.money = money;
	}


	//メソッド=================================
	public void action() {
		cntct++;
		account();
	};
	
	public void popuStats(CharaBuild cb, double hitP) {//人口統計
		popuMap.put(cb, (int)hitP);//書き換えたいのでputIfAbsentじゃない
	}
	
	public void popuRemove(CharaBuild cb) {
		popuMap.remove(cb);
	}
	
	private void account() {//経理。popuStatsに入れてもいいが、税金管理はそれほど頻繁でなくていいので別。
		if(cntct > lptim()) {
			popula = 0;
			for(Integer i : popuMap.values()) {
				popula = popula + i;
			}
	
			moneyD = popula * taxrate / 110;
			money = money + (int)moneyD;			
			cntct = 0;
		}
	}
	
	public void billExpense() {
		money = money - 100;
	}
	
	
	//ゲッター
	public int getTax() {
		return taxrate;
	}
	
	public int getMoney() {
		return money;
	}

	public void setTax(int t) {
		taxrate = t;
	}

	public void setMoney(int m) {
		money = m;
	}
	
	
	
	
	
	
	
	void print(Object obj, Object obj2) {
		System.out.println("  CHARA TEAM  " + obj + obj2);
		System.out.println();
	}

	void print(Object obj) {//Overrode
		System.out.println("  CHARA TEAM  " + obj);
		System.out.println();
	}

}