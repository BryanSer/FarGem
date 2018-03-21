/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Br.FarGem;

/**
 *
 * @author Bryan_lzh
 */
public interface GemRunnable {

    /**
     * 每次运行时将调用该方法
     */
    public void run();

    /**
     * 运行间隔
     * @return
     */
    public long interval();

    /**
     * 运行延迟
     * @return
     */
    public long delay();
}
