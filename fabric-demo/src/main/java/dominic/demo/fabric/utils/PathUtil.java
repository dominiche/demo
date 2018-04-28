package dominic.demo.fabric.utils;

public interface PathUtil {
    /**
     * 注意，这是的路径只对在IDE环境中直接运行和dubug有效，打成jar运行的话会找不到。
     * 如果要解决这俩个不同情况的话，java代码中的路径可以通过classLoad的getResource按相对路径来解决，
     * 但是network-config.yaml文件中的密钥文件路径只能写字符串形式的路径，用相对路径的话在IDE中debug与打成jar包中的又不一样，
     * 所以最后妥协一下，统一只按在ide中直接运行和debug的相对路径来写。
     * 该项目只是作为演示作用, 为方便还把fabric网络的相关配置文件等放到了resource中，如果是打成jar包在生产运行的话，请自行调整。
     * 一种方案是把network-config写成json形式，存到数据库中，json中的密钥文件路径就使用在服务器中的绝对路径。
     */
    String parentPath = "fabric-demo/src/main/resources/";
}
