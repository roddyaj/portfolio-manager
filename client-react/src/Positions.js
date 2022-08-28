import { useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import { renderPositionPopup } from './PositionPopup'

function Positions(props) {
	const { portfolio } = props;

	const [mode, setMode] = useState("view");

	let viewPositions = portfolio.positions.filter(p => !p.symbol.includes(" "));
	if (mode === "view") {
		viewPositions = viewPositions.sort((a, b) => b.marketValue - a.marketValue);
	} else if (mode === "shares") {
		viewPositions = viewPositions.filter(p => p.sharesToBuy).sort((a, b) => (a.sharesToBuy * a.price) - (b.sharesToBuy * b.price));
	} else if (mode === "calls") {
		viewPositions = viewPositions.filter(p => p.callsToSell).sort((a, b) => b.dayChangePct - a.dayChangePct);
	}

	const showAllPositions = mode === "view";

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">
					<input type="radio" name="positionMode" id="mode-view" value="view" checked={mode === "view"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-view" style={{ paddingLeft: 3, marginRight: 10 }}>Positions</label>
					<input type="radio" name="positionMode" id="mode-trade-shares" value="shares" checked={mode === "shares"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-trade-shares" style={{ paddingLeft: 3, marginRight: 10 }}>Trades</label>
					<input type="radio" name="positionMode" id="mode-sell-calls" value="calls" checked={mode === "calls"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-sell-calls" style={{ paddingLeft: 3, marginRight: 10 }}>Calls to Sell</label>
				</div>
				<span>({viewPositions.length})</span>
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
						{mode !== "calls" && <th>Ratio</th>}
						{mode === "shares" && <th className="c">Trade</th>}
						{mode !== "calls" && <th className="c">{mode === "view" ? "Open" : ""}</th>}
						{mode === "calls" && <th className="c">Sell Calls</th>}
						<th></th>
					</tr>
				</thead>
				<tbody>
					{viewPositions.map(p => renderRow(p, mode))}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position, mode) {
	const showAllPositions = mode === "view";
	const action = position.sharesToBuy > 0 ? "Buy" : "Sell";
	const actionText = `${action} ${Math.abs(position.sharesToBuy)}`;
	const actionUrl = `https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=${action}&Symbol=${position.symbol}`;
	const schwabOpenOrdersUrl = "https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open";
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
			{showAllPositions && <td>{position.percentOfAccount ? position.percentOfAccount.toFixed(2) + "%" : ""}</td>}
			{showAllPositions && <td>{position.targetPct ? position.targetPct.toFixed(2) + "%" : ""}</td>}
			{mode !== "calls" && <td>{position.percentOfAccount && position.targetPct ? (100 * position.percentOfAccount / position.targetPct).toFixed(1) + "%" : ""}</td>}
			{mode === "shares" && <td className="c"><a href={actionUrl}><button style={{ minWidth: 55 }} onClick={() => copyClip(Math.abs(position.sharesToBuy))}>{actionText}</button></a></td>}
			{mode !== "calls" && <td className="c"><a href={schwabOpenOrdersUrl}>{openOrderText}</a></td>}
			{mode === "calls" && <td className="c"><a href={optionChainUrl}><button>Sell {position.callsToSell}</button></a></td>}
			<OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={renderPositionPopup(position)}>
				<td><i className="bi bi-info-circle" style={{ marginLeft: 6 }}></i></td>
			</OverlayTrigger>
		</tr>
	);
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}

export default Positions;
