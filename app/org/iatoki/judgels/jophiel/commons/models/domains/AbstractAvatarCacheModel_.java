package org.iatoki.judgels.jophiel.commons.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AbstractAvatarCacheModel.class)
public abstract class AbstractAvatarCacheModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<AbstractAvatarCacheModel, Long> id;
    public static volatile SingularAttribute<AbstractAvatarCacheModel, String> userJid;
    public static volatile SingularAttribute<AbstractAvatarCacheModel, String> avatarUrl;
}