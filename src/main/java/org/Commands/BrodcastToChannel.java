package org.Commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import org.Utiilities.IStock;

public class BrodcastToChannel extends AbstractCommand implements IStock {
    boolean isMasterinStock = false;
    boolean isTravellingMerchantPreasent = false;
    @Override
    public void execute(Message message, GatewayDiscordClient client) {

    }

    @Override
    public void setMasterInStock(boolean isMasterInStock) {
        this.isMasterinStock = isMasterInStock;
    }

    @Override
    public void setTravellingMerchant(boolean isTravellingMerchantPresent) {
        this.isTravellingMerchantPreasent = isTravellingMerchantPresent;

    }
    @Override
    public void reset() {
    this.isTravellingMerchantPreasent = false;
    this.isMasterinStock = false;
    }
}
