/*使い方
//使用するソースファイルに
	import img.Img;

	Img img = new Img();
	img.printAllPath();
	ImageView view = img.getView(img.allPath(i));

*/
/*コンパイル

当クラス、コンパイル時のコード
	cd $IMG
	javac ~中略~ Img.java($IMGのPATHあり)
	*ソースパス、クラスパスはいらない

使用クラスの場合
	javac ~中略~ -cp $SOURCE [使用クラス名]

*/

package pictpack;

import java.io.*;
import java.util.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Img {

	private String absoPath = "/Users/yngt/program/monajava/src/imgmod/pictpack/";
	
	public String[] allPath;
	public String[] hakoPath;
	public String[] charaPath;
	public String[] kaokuPath;
	
	
	//コンストラクタ
	public Img() {

		hakoPath = new String[] {
			"nanamehakoao.png",
			"nanamehako.png",
			"nanamehakoaka.png"
		};
		
		charaPath = new String[] {
			"jurokuA32+3.png",
			"jurokuB32+3.png",
			"p128.png",
			"man04.png",
			"teki.png",
			"man05.png"
		};
		
		kaokuPath = new String[] {
			"kaoku01.png",
			"kaoku02.png",
			"kaoku03.png"
		};
		
		allPath = new String[] {
			"nanamehakoao.png",
			"nanamehako.png",
			"nanamehakoaka.png",
			"jurokuA32+3.png",
			"jurokuB32+3.png",
			"p128.png",
			"man04.png",
			"teki.png",
			"man05.png",
			"kaoku01.png",
			"kaoku02.png",
			"kaoku03.png"
		};
		
	}//コンスト
	
	public ImageView getView(String path) {
		return new ImageView( getImg(path) );
	}
	
	public Image getImg(String path) {//メインクラスでimg.xxPath
		return new Image(getFile(path).toURI().toString());
	}
	
	public File getFile(String path) {
		String newPath = absoPath + path;
		return new File(newPath);
	}

	public String getFilePath(File f) {
		return f.toURI().toString();
	}
	
	
	public void printAllPath() {
		for(String path : allPath) {
			print(path);
		}
	}
	
	private void print(Object obj) {
		System.out.println("  IMG  " + obj);
	}

	private void print(Object obj, Object obj2) {
		System.out.println("  IMG  " + obj +"  "+ obj2);
	}	
}

