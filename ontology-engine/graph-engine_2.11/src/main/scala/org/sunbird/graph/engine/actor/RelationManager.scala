package org.sunbird.graph.engine.actor

import org.apache.commons.collections4.MapUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.actor.router.ActorConfig
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.common.exception.{ClientException, ResponseCode, ServerException}
import org.sunbird.graph.mgr.BaseGraphManager
import org.sunbird.graph.model.IRelation

@ActorConfig(tasks = Array("createNewRelations"))
class RelationManager extends BaseGraphManager {
    @throws[Exception]
    override def onReceive(request: Request): Unit = {
        val operation = request.getOperation
        operation match {
            case "createNewRelations" =>
                createNewRelations(request)
            case _ =>
                ERROR(operation)
        }
    }

    @throws[Exception]
    def createNewRelations(request: Request): Unit = {
        val relations: List[IRelation] = request.get("relations").asInstanceOf[List[IRelation]]

        relations.foreach( relation => {
            val req = new Request()
            req.setContext(request.getContext)
            val errorMap =  relation.validateRelation(req)
            if(MapUtils.isNotEmpty(errorMap)){
                println(errorMap)
                throw new ClientException(ResponseCode.CLIENT_ERROR.name, "Error while validating relations :: " + errorMap)
            }

            }
        )
        relations.foreach(relation => {
            val req = new Request()
            req.setContext(request.getContext)
            val msg = relation.createRelation(req)
            if(StringUtils.isNotBlank(msg))
                throw new ServerException(ResponseCode.SERVER_ERROR.name(), "Error while creating relation :: " +  msg)
        })
        OK(sender())
    }
}
