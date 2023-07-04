package monarch;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;


public class HeadUpDisplay {
	static Pane p = new Pane();
	//Group p = new Group();
	//Region p = new Region();
	GridPane gridP = new GridPane();
	VBox vbox;//垂直
	HBox hbox;//水平
	Rectangle rec;
	static Label[] labelTitle = new Label[3];
	static Label[] labelInt = new Label[3];
	
	//スライダー＝kb.hagukumi(), cb.移動スピード。初期値
	static Slider slidCb = new Slider(30, 120, 60);//35で固定。あとはCMのlpSpeedで変更
	static Slider slidKb = new Slider(0, 30, 10);
	static Slider slidLs = new Slider(1, 2, 1);//最大値を基本地にして、数値を減らして速くする
	static Slider[] slid = {//(min,max,now)
		new Slider(30, 120, 90),//cb.lptim
		new Slider(0, 30, 10)//kb.taxrate
	};
	
	//コンストラクタ
	public HeadUpDisplay() {
		p.setId(""+4);
	}

	public void action() {
		labelTitle[0] = new Label("CB.SET TIME");
		labelTitle[1] = new Label("KB.SET TAX");
		labelTitle[2] = new Label("CM.LPSPEED");
		labelInt[0] = new Label("0");
		labelInt[1] = new Label("0");
		labelInt[2] = new Label("0");
		
		
		gridP.add(labelTitle[0], 0, 0);
		gridP.add(labelTitle[1], 0, 1);
		gridP.add(labelTitle[2], 0, 2);
		gridP.add(labelInt[0], 1, 0);
		gridP.add(labelInt[1], 1, 1);
		gridP.add(labelInt[2], 1, 2);
		gridP.add(slidCb, 2, 0);
		gridP.add(slidKb, 2, 1);
		gridP.add(slidLs, 2, 2);

		setSlider(slidCb, true, true, 10, 0);
		setSlider(slidKb, true, true, 10, 4);
		setSlider(slidLs, true, true, 0.5, 0);
		slidCb.setBlockIncrement(30);//この値分移動。ただしsnapToTicksに制限される

		p.getChildren().addAll(gridP);
	}

	public void setTime(int index, int newTime) {
		labelInt[index].setText(String.valueOf(newTime));
	}
	
	public void setTime(int index, double newTime) {
		labelInt[index].setText(String.valueOf(newTime));
	}
	
	
	public void setSlider(Slider sl, boolean bool1, boolean bool2, double numBig, int numSml) {
		sl.setShowTickLabels(true);//数値表示
		sl.setShowTickMarks(bool1);//目盛り有無
		sl.setSnapToTicks(bool2);//値を常に目盛りにあわせるか
		sl.setMajorTickUnit(numBig);//大目盛りの距離
		sl.setMinorTickCount(numSml);//二つの大目盛りの間にある小目盛り
		
		//slid.setBlockIncrement(10);//この値分移動。ただしsnapToTicksに制限される
		//slid.increment();//BlockIncrement分移動。eventなどで使う
		//slid.decrement();
	}
	
	
	
	
	
	private void print(Object obj, Object obj2) {
		System.out.println("  HUD  " + obj + obj2);
		System.out.println();
	}

	private void print(Object obj) {//Overrode
		System.out.println("  HUD  " + obj);
		System.out.println();
	}

}