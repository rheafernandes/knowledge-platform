package org.sunbird.graph.engine

import org.apache.commons.collections4.MapUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, Response, ResponseParams}
import org.sunbird.common.exception.{ClientException, ResponseCode, ServerException}
import org.sunbird.graph.model.IRelation

import scala.concurrent.{ExecutionContext, Future}


object RelationManager {

    @throws[Exception]
    def createNewRelations(request: Request)(implicit ec: ExecutionContext): Future[Response] = {
        val relations: List[IRelation] = request.get("relations").asInstanceOf[List[IRelation]]

        relations.foreach(relation => {
            val req = new Request()
            req.setContext(request.getContext)
            val errorMap =  relation.validateRelation(req)
            if(MapUtils.isNotEmpty(errorMap)){
                throw new ClientException(ResponseCode.CLIENT_ERROR.name, "Error while validating relations :: " + errorMap)
            }
        })
        relations.foreach(relation => {
            val req = new Request()
            req.setContext(request.getContext)
            val msg = relation.createRelation(req)
            if(StringUtils.isNotBlank(msg))
                throw new ServerException(ResponseCode.SERVER_ERROR.name(), "Error while creating relation :: " +  msg)
        })
        Future(new Response)
    }
}
