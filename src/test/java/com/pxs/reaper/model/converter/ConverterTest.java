package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.toolkit.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.CpuPerc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.persistence.Convert;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ConverterTest {

    private PodamFactory factory;
    @Spy
    private CpuArrayConverter cpuArrayConverter;

    @Before
    public void before() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void convertAllEntities() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        convertAllEntities(JMetrics.class);
        convertAllEntities(OSMetrics.class);

        Constructor constructor = CpuPerc.class.getDeclaredConstructors()[0];
        constructor.setAccessible(Boolean.TRUE);
        CpuPerc cpuPerc = (CpuPerc) constructor.newInstance();
        convertEntity(CpuPercArrayConverter.class.newInstance(), new CpuPerc[]{cpuPerc});
    }

    @SuppressWarnings("unchecked")
    private void convertAllEntities(final Class<?> clazz) {
        ReflectionUtils.doWithFields(clazz, field -> {
            Convert convert = field.getAnnotation(Convert.class);
            if (convert == null) {
                return;
            }
            try {
                if (field.getType().getSimpleName().contains(CpuPerc.class.getSimpleName())) {
                    return;
                }
                GenericConverter genericConverter = (GenericConverter) convert.converter().newInstance();
                Object entity = factory.manufacturePojoWithFullData(field.getType());
                convertEntity(genericConverter, entity);
            } catch (InstantiationException e) {
                log.error(null, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void convertEntity(final GenericConverter genericConverter, final Object entity) {
        log.info("Converter : {}, {}", genericConverter, entity.getClass());
        String json = genericConverter.convertToDatabaseColumn(entity);
        log.info("Json : {}", json);
        Object deserialized = genericConverter.convertToEntityAttribute(json);
        log.info("Deserialized : {}", deserialized);
    }

}
