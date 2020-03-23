package org.vkp.racing.ecs.system;

import java.lang.reflect.Field;
import java.util.List;

import org.vkp.racing.ecs.component.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ComponentExtractor {

	private ComponentExtractor() {

	}

	public static Component extractOne(Class<?> type, List<Component> components) {
		for (Component component : components) {
			if (component.getClass() == type) return component;
		}

		return null;
	}

	public static <T> void extract(T componentGroup, List<Component> components) {
		components.forEach(component -> setMatchingField(componentGroup, component));
	}

	private static <T> void setMatchingField(T componentGroup, Component component) {
		Field[] fields = componentGroup.getClass().getFields();

		for (Field field : fields) {
			if (field.getType() == component.getClass()) {
				try {
					field.set(componentGroup, component);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.catching(e);
				}

				break;
			}
		}
	}

}
