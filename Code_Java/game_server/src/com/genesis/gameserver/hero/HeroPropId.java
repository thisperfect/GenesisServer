package com.genesis.gameserver.hero;

import com.genesis.common.prop.IProp;
import com.genesis.common.prop.PropScope;
import com.genesis.common.prop.PropType;

import java.util.Map;

public enum HeroPropId implements IProp {
    /** 英雄可成长属性*/
    LEVEL(0, Long.MAX_VALUE), EXP(0, Long.MAX_VALUE),
    /** 英雄不可成长属性 */

    ;

    private static Map<String, HeroPropId> reflect =
            PropType.constructReflect(HeroPropId.class, PropType.HERO);
    public final PropScope scope;

    HeroPropId(long minValue, long maxValue) {
        this.scope = new PropScope(minValue, maxValue);
    }

    /**
     * 根据属性数字Id获取对应的HeroChangeablePropId
     * @param idName
     * @return
     */
    public static HeroPropId get(int idName) {
        return reflect.get(idName);
    }

    /**
     * 判断指定的索引是否有被定义
     * @param idIndex
     * @return
     */
    public static boolean containsIndex(int idIndex) {
        return reflect.containsKey(idIndex);
    }

    @Override
    public PropType getPropType() {
        return PropType.HERO;
    }

}
