package de.robotricker.electricwires.utils.staticutils;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

public class ProtocolUtils {

	public static void createParticle(Particle particle, World world, float x, float y, float z, float xD, float yD, float zD, float speed, int[] data, boolean farVisible, int amount) {
		WrapperPlayServerWorldParticles wrapperParticles = new WrapperPlayServerWorldParticles();
		wrapperParticles.setParticleType(particle);
		wrapperParticles.setX(x);
		wrapperParticles.setY(y);
		wrapperParticles.setZ(z);
		wrapperParticles.setOffsetX(xD);
		wrapperParticles.setOffsetY(yD);
		wrapperParticles.setOffsetZ(zD);
		wrapperParticles.setLongDistance(farVisible);
		wrapperParticles.setData(data);
		wrapperParticles.setNumberOfParticles(amount);
		wrapperParticles.setParticleData(speed);

		for (Player p : world.getPlayers()) {
			wrapperParticles.sendPacket(p);
		}
	}

}
