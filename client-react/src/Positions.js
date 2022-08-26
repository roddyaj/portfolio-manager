import { useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import { renderPositionPopup } from './PositionPopup'

function Positions(props) {
	const { portfolio } = props;

	const [showAllPositions, setShowAllPositions] = useState(true);

	const viewPositions = portfolio.positions.filter(p => !p.symbol.includes(" ") && (showAllPositions || p.sharesToBuy));
	if (showAllPositions) {
		viewPositions.sort((a, b) => b.marketValue - a.marketValue);
	} else {
		viewPositions.sort((a, b) => (a.sharesToBuy * a.price) - (b.sharesToBuy * b.price));
	}

	if (viewPositions.length === 0) {
		return null;
	}

	const showCallsToSell = viewPositions.filter(p => p.callsToSell > 0).length > 0;

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Positions ({viewPositions.length})</div>

				<div style={{ marginLeft: 12 }}>
					<input type="radio" name="positionVisibility" id="visibility-all" value="all" checked={showAllPositions} onChange={() => setShowAllPositions(true)} />
					<label htmlFor="visibility-all" style={{ padding: 2, marginRight: 4 }}>All</label>
					<input type="radio" name="positionVisibility" id="visibility-actions" value="actions" checked={!showAllPositions} onChange={() => setShowAllPositions(false)} />
					<label htmlFor="visibility-actions" style={{ padding: 2, marginRight: 4 }}>Actions</label>
				</div>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l">Ticker</th>
						<th></th>
						{showAllPositions && <th>#</th>}
						<th>Price</th>
						{showAllPositions && <th>Value</th>}
						<th>Day</th>
						<th>G/L</th>
						{showAllPositions && <th>Actual</th>}
						{showAllPositions && <th>Target</th>}
						<th>Ratio</th>
						{!showAllPositions && <th className="l">Action</th>}
						<th className="c">Open</th>
						{!showAllPositions && showCallsToSell && <th className="c">Calls to Sell</th>}
						<th></th>
					</tr>
				</thead>
				<tbody>
					{viewPositions.map(p => renderRow(p, showAllPositions, showCallsToSell))}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position, showAllPositions, showCallsToSell) {
	const action = position.sharesToBuy > 0 ? "Buy" : "Sell";
	const actionText = `${action} ${Math.abs(position.sharesToBuy)}`;
	const actionUrl = `https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=${action}&Symbol=${position.symbol}`;
	const optionChainUrl = `https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/${position.symbol}`;
	const openBuyCount = position.openOrders ? position.openOrders.filter(o => o.action === "Buy").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
	const openSellCount = position.openOrders ? position.openOrders.filter(o => o.action === "Sell").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
	const openOrderArray = [['B', openBuyCount], ['S', openSellCount]].filter(a => a[1] !== 0);
	const openOrderText = showAllPositions
		? openOrderArray.map(a => a.join(" ")).join(", ")
		: openOrderArray.filter(a => a[0] === action.charAt(0)).map(a => a[1]).join("");

	return (
		<tr key={position.symbol}>
			<td className="l">
				<a href={`https://client.schwab.com/SymbolRouting.aspx?Symbol=${position.symbol}`} style={position.peRatio < 0 ? { backgroundColor: "#FDD" } : {}}>{position.symbol}</a>
			</td>
			<td>
				<a href={`https://finance.yahoo.com/quote/${position.symbol}`}>
					<img src="https://s.yimg.com/cv/apiv2/default/icons/favicon_y19_32x32_custom.svg" alt="Yahoo" width={14} height={14} />
				</a>
			</td>
			{showAllPositions && <td>{position.quantity}</td>}
			<td>{position.price.toFixed(2)}</td>
			{showAllPositions && <td>{position.marketValue.toFixed(2)}</td>}
			<td style={{ color: position.dayChangePct >= 0 ? "green" : "#C00" }}>{Math.abs(position.dayChangePct).toFixed(2) + "%"}</td>
			<td style={{ color: position.gainLossPct >= 0 ? "green" : "#C00" }}>{Math.abs(position.gainLossPct).toFixed(2) + "%"}</td>
			{showAllPositions && <td>{position.percentOfAccount.toFixed(2) + "%"}</td>}
			{showAllPositions && <td>{position.targetPct ? position.targetPct.toFixed(2) + "%" : ""}</td>}
			<td>{position.targetPct ? (100 * position.percentOfAccount / position.targetPct).toFixed(1) + "%" : ""}</td>
			{!showAllPositions && <td className="l">{position.sharesToBuy ? (<a href={actionUrl} onClick={() => copyClip(Math.abs(position.sharesToBuy))}>{actionText}</a>) : ""}</td>}
			<td className="c"><a href={schwabOpenOrdersUrl}>{openOrderText}</a></td>
			{!showAllPositions && showCallsToSell && <td className="c">{position.callsToSell ? (<a href={optionChainUrl}>{position.callsToSell}</a>) : ""}</td>}
			<OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={renderPositionPopup(position)}>
				<td><i className="bi bi-info-circle"></i></td>
			</OverlayTrigger>
		</tr>
	);
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}

const schwabOpenOrdersUrl = "https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open";

export default Positions;
